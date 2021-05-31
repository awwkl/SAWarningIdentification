package com.warningClassification;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Random;

import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayes;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSink;
import weka.core.converters.ConverterUtils.DataSource;

public class FeatureEvaluation {

	//���Ѿ�ѡ�������feature�������򣬸�����Ԥ���Ч����
		//��Ϊ��̫���weka������˳����ʲô˳����֤һ�¡��п���weka�����ľ�������֮���	
	public void selectedFeatureRank (  String fileName){		
		try {
			File folder = new File ( "data/feature/" );
			String[] warningInfoList = folder.list();
			for ( int k =0; k < warningInfoList.length; k++ ){
				File projectFolder = new File ( folder + "/" + warningInfoList[k] );
				if ( !projectFolder.isDirectory() )
					continue;
				
				String folderName = folder + "/" + warningInfoList[k];
				
				ArrayList<String> selectedFeatureList = this.obtainSelectedFeatureList( folderName + fileName);
				// "/featureRank.csv"

				HashMap<String, Double> featureEvaluation = new HashMap<String, Double>();
				for ( int i =0; i < selectedFeatureList.size(); i++ ){
					//ȥ��selectedFeatureList[i]
					//���� folderName + "/newTotalFeatures.csv"���������ڷ���
					
					ArrayList<String> refinedSelectedFeatureList = new ArrayList<String>();
					for ( int j =0; j < selectedFeatureList.size(); j++ ){
						if ( i ==j )
							continue;
						refinedSelectedFeatureList.add( selectedFeatureList.get( j ));
					}
					String fileTrain = this.generateFeatureValueBasedSelectedFeatures(folderName, refinedSelectedFeatureList );
					
					Double accuracy = this.conductPrediction( fileTrain );
					featureEvaluation.put( selectedFeatureList.get( i ), accuracy );
				}
				
				BufferedWriter output = new BufferedWriter ( new OutputStreamWriter ( new FileOutputStream ( new File (  folderName + "selected" + fileName )) , "GB2312"), 1024);
				
				for ( String feature: featureEvaluation.keySet()){
					output.write( feature + "," + featureEvaluation.get( feature ));
					output.newLine();
				}
				output.flush();
				output.close();
			}
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public ArrayList<String> obtainSelectedFeatureList ( String fileName ){
		ArrayList<String> selectedFeatureList = new ArrayList<String>();
		try {
			BufferedReader br = new BufferedReader(new FileReader( new File ( fileName )));
			String line = "";
			while ( ( line = br.readLine() ) != null ) {
				String[] temp = line.split( ",");
				if ( temp.length != 3 ){
					continue;
				}
				if ( !temp[2].trim().equals( "category"))
					selectedFeatureList.add( temp[2].trim() );
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return selectedFeatureList;
	}
	
	//����totalFeatures�������е�features���Լ�selectedFeatureName������Ҫѡ���featureName�������µĿ�������weka������ļ�
	public String generateFeatureValueBasedSelectedFeatures ( String folderName, ArrayList<String> selectedFeatureName ){
		try {
			HashMap<String, Integer> featureNameMap = new HashMap<String, Integer>();
			HashMap<Integer, ArrayList<String>> featureValueMap = new LinkedHashMap<Integer, ArrayList<String>>();
			
			BufferedReader br = new BufferedReader(new FileReader( new File ( folderName + "/" + "totalFeatures.csv" )));
			String line = "";
			boolean featureName = false;
			while ( ( line = br.readLine() ) != null ) {
				String[] temp = line.split( ",");
				if ( temp.length <= 0 )
					continue;
				if ( featureName == false){
					featureName = true;
					for ( int i =0; i < temp.length; i++ ){
						ArrayList<String> featureValue = new ArrayList<String>();
						featureValueMap.put( i, featureValue );
						featureNameMap.put( temp[i], i );
					}
				}
				else{
					for ( int i =0; i < temp.length; i++ ){
						ArrayList<String> featureValue = featureValueMap.get( i );
						featureValue.add( temp[i]);
						featureValueMap.put( i , featureValue );
					}
				}
			}
			br.close();
			
			String fileTrain = folderName + "/newTotalFeatures.csv";
			BufferedWriter output = new BufferedWriter ( new OutputStreamWriter ( new FileOutputStream ( new File ( fileTrain )) , "GB2312"), 1024);
			for ( int j =0; j < selectedFeatureName.size(); j++ ){
				output.write( selectedFeatureName.get( j ) + ",");
			}
			output.write( "category");
			output.newLine();
			//k��ʾinstance����Ŀ����Ҫ�������ݵ�����
			for ( int k =0; k < featureValueMap.entrySet().iterator().next().getValue().size(); k++ ){
				for ( int j =0; j < selectedFeatureName.size(); j++ ){	
					if ( featureNameMap.containsKey( selectedFeatureName.get( j) ) ){
						int index = featureNameMap.get( selectedFeatureName.get( j ));
						ArrayList<String> featureValue = featureValueMap.get( index );
						output.write( featureValue.get( k) + ",");
					}else{
						output.write( "0" + ",");
					}
				}
				int index = featureNameMap.get( "category");
				ArrayList<String> featureValue = featureValueMap.get( index );
				output.write( featureValue.get( k) );
				
				output.newLine();
			}
			output.flush();
			output.close();
			
			return fileTrain;
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public Double conductPrediction ( String fileTrain ){
		try {
		    Instances data = DataSource.read( fileTrain );	    
		    //Ĭ����ĩ����category
		    data.setClassIndex( data.numAttributes() - 1 );
		    
		    Evaluation evaluation  = new Evaluation( data );
		    
		    //DataSink.write( "data/feature/weka.arff", data );
		    
		    NaiveBayes classify = new NaiveBayes() ;   
		    String[] options = {};
		    classify.setOptions(options);
		 
		    evaluation.crossValidateModel( classify, data, 10, new Random(1));
		    
		    System.out.println ( evaluation.toSummaryString("/nResults/n======/n", true) );
			//System.out.println ( evaluation.toClassDetailsString() );
			//System.out.println ( evaluation.toMatrixString(  ) );
			
		    //Double score = evaluation.weightedFMeasure();
		    //Double score = (1.0* (evaluation.truePositiveRate( 0) + evaluation.trueNegativeRate( 0) ) ) / (evaluation.truePositiveRate( 0) + 
		    //		evaluation.trueNegativeRate( 0) + evaluation.falsePositiveRate(0) + evaluation.falseNegativeRate(0));
		    
		    Double score = evaluation.fMeasure(1);
		    //Double score = (1.0* evaluation.truePositiveRate( 0) ) / (evaluation.truePositiveRate( 0) +  evaluation.falsePositiveRate(0) ); precision
		    //Double score = evaluation.fMeasure(1);
		    return score;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}	
		
	/*
	 * ������ͬfeatureSet�����ܣ�������
	 * 1. ������Ŀ��ͬѡ�������featureSet
	 * 2. ���ڵ���revisionѡ�������featureSet
	 * 3. ����������ѡ���featureSet
	 * 4. ����i��revisionѡ�������featureSet������i+1��revision
	 */
	public void featureSetEvaluation ( ){
		try {
			BufferedWriter output = new BufferedWriter ( new OutputStreamWriter ( new FileOutputStream ( new File ( "data/feature/featureEvaluation.csv" )) , "GB2312"), 1024);
			output.write( "formerFolderName" + "," + "folderName" + ",");
			output.write( "accuracyCommon" + "," + "accuracySingle" + "," + "accuracyTotal" + "," + "accuracyNext" );
			output.newLine();
			
			File folder = new File ( "data/feature" );
			String[] warningInfoList = folder.list();
			for ( int i =0; i < warningInfoList.length; i++ ){
				File projectFolder = new File ( folder + "/" + warningInfoList[i] );
				if ( !projectFolder.isDirectory() )
					continue;
				
				String folderName = folder + "/" + warningInfoList[i];
				Double accuracyCommon = this.commonFeatureSetEvaluation(folderName);
				
				Double accuracySingle = this.singleRevisionFeatureSetEvaluation( folderName );
				Double accuracyTotal = this.totalFeatureSetSelection(folderName);
				
				Double accuracyNext = 0.0;
				String formerFolderName = "";
				if ( i > 0 ){
					formerFolderName = folder  + "/" + warningInfoList[i-1];
					accuracyNext = this.nextRevisionFeatureSetSelection( formerFolderName, folderName);
				}
				
				output.write( formerFolderName + "," + folderName + ",");
				output.write( accuracyCommon + "," + accuracySingle + "," + accuracyTotal + "," + accuracyNext );
				output.newLine();
			}
			
			output.flush();
			output.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public Double commonFeatureSetEvaluation( String folderName ){
		//�����ǵõ��ܵ�feature
		ArrayList<String> selectedFeatureList = this.obtainSelectedFeatureList( "data/feature/featureRankCombine.csv");
		ArrayList<String> selectedFeatureListSingle = this.obtainSelectedFeatureList( folderName + "/featureRank.csv");
		/*
		 * selectedFeatureList ΪF3���ָ����ͣ�����generateFeatureValueBasedSelectedFeatures�õ�����F3-String�������ͣ�
		 * ������Ҫ������Ĳ�������
		 */
		ArrayList<String> refinedSelectedFeatureList = new ArrayList<String>();
		for ( int i =0; i < selectedFeatureListSingle.size(); i++ ){
			String feature = selectedFeatureListSingle.get( i );
			if ( selectedFeatureList.contains( feature )){
				refinedSelectedFeatureList.add( feature );
			}
			else{
				int index = feature.indexOf( "-");
				String newFeature = "";
				if ( index > 0 ){
					newFeature = feature.substring( 0, index);
				}
				if ( selectedFeatureList.contains( newFeature)){
					refinedSelectedFeatureList.add( feature );
				}
			}			
		}
		
		String fileTrain = this.generateFeatureValueBasedSelectedFeatures(folderName, refinedSelectedFeatureList );
		
		Double accuracy = this.conductPrediction(fileTrain);
		System.out.println( "The prediction accuracy of commonFeatureSetEvaluation in " + folderName + " is : " + accuracy );
		
		return accuracy;
	}
	
	public Double singleRevisionFeatureSetEvaluation ( String folderName ){
		//�õ����ļ����ڲ���featureRank
		ArrayList<String> selectedFeatureList = this.obtainSelectedFeatureList( folderName + "/featureRank.csv");
		
		String fileTrain = this.generateFeatureValueBasedSelectedFeatures(folderName, selectedFeatureList );
		
		Double fMeasure = this.conductPrediction(fileTrain);
		System.out.println( "The prediction fMeasure of singleRevisionFeatureSetEvaluation in " + folderName + " is : " + fMeasure );
		
		return fMeasure;
	}
	
	public Double totalFeatureSetSelection( String folderName ){
		String fileTrain = folderName + "/totalFeatures.csv";
		Double accuracy = this.conductPrediction(fileTrain);
		System.out.println( "The prediction accuracy of totalFeatureSetSelection in " + folderName + " is : " + accuracy );
		
		return accuracy;
	}
	
	
	public Double nextRevisionFeatureSetSelection( String folderNameFormer, String folerNameLatter ){
		ArrayList<String> selectedFeatureList = this.obtainSelectedFeatureList( folderNameFormer + "/featureRank.csv");
		
		String fileTrain = this.generateFeatureValueBasedSelectedFeatures(folerNameLatter, selectedFeatureList );
		
		Double accuracy = this.conductPrediction(fileTrain);
		System.out.println( "The prediction accuracy of nextRevisionFeatureSetSelection in " + folerNameLatter + " is : " + accuracy );
		
		return accuracy;
	}
	
	public static void main ( String args[] ){
		FeatureEvaluation evaluation = new FeatureEvaluation();
		// evaluation.selectedFeatureRank( "/featureRank.csv" );
		evaluation.singleRevisionFeatureSetEvaluation( "data/feature" );
		
		/*
		 * ��Ҫע����ǣ�featureRankCombineҪת���ɺ͵�����Ŀ��featureRank���Ƶ���ʽ�����У���������feature name
		 */
		evaluation.featureSetEvaluation();
	}
}
