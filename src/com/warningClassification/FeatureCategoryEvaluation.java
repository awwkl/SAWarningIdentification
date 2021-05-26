package com.warningClassification;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Random;

import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayes;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;

public class FeatureCategoryEvaluation {
	
	//һ�����͵�feature set��evaluation
	public void featureSetEvaluation( String featureCategory ){
		ArrayList<String> selectedFeatureList = new ArrayList<String>();
		//�õ���������F3�������͵ģ�ʵ����totalFeature����F3-**����Ҫ������������ҵ�
		BufferedReader brCategory;
		try {
			brCategory = new BufferedReader(new FileReader( new File ( "resources/feature_category.csv" ) ));
			String line = "";
			while ( (line = brCategory.readLine()) != null ){
				line = line.trim();
				if ( line.equals( ""))
					continue;
				
				String[] temp = line.split( ",");
				if ( temp.length != 2 )
					continue;
				
				int featureId = Integer.parseInt( temp[0] );
				String category =  temp[1].trim();
				if ( featureCategory.equals( category )){
					selectedFeatureList.add( "F" + featureId );
				}
			}
			brCategory.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
				
		try {
			BufferedWriter output = new BufferedWriter ( new OutputStreamWriter ( new FileOutputStream ( new File ( "data/feature/" + featureCategory + "-featureCategoryEvaluation.csv" )) , "GB2312"), 1024);

			File folder = new File ( "data/feature" );
			String[] warningInfoList = folder.list();
			for ( int i =0; i < warningInfoList.length; i++ ){
				File projectFolder = new File ( folder + "/" + warningInfoList[i] );
				if ( !projectFolder.isDirectory() )
					continue;
				
				String folderName = folder + "/" + warningInfoList[i];
				String fileTrain = this.generateFeatureValueBasedSelectedFeatures( folderName, selectedFeatureList );
				
				Double accuracy = this.conductPrediction(fileTrain);
				System.out.println( "The prediction accuracy of category " + featureCategory + " in " + folderName + " is : " + accuracy );
				
				output.write( folderName + "," + accuracy + ",");
				output.newLine();
			}
			output.flush();
			output.close();
			
		} catch (UnsupportedEncodingException | FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	////����totalFeatures�������е�features���Լ�selectedFeatureName������Ҫѡ���featureName�������µĿ�������weka������ļ�
	public String generateFeatureValueBasedSelectedFeatures ( String folderName, ArrayList<String> selectedFeatureName ){
		try {
			/*
			 * �洢���Ǹð汾�е�fullFeatureList��selectedFeatureName�ܵ���F3��refinedSelectedFeatureName�д洢���Ǹ���Ŀ�о����F3-**,
			 * ��Ҫ�ڸ�����Ŀ�е���ͳ�ƣ���Ϊÿ����Ŀ���ܻ᲻һ��
			 */
			
			ArrayList<String> refinedSelectedFeatureName = new ArrayList<String>();
			
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
						String featureFullName = temp[i].trim();
						String featureShortName = featureFullName;
						int index = featureFullName.indexOf( "-");
						if ( index > 0 ){
							featureShortName = featureFullName.substring( 0, index);
						}
						if ( selectedFeatureName.contains( featureShortName) ) {
							refinedSelectedFeatureName.add( featureFullName );
						}
						
						ArrayList<String> featureValue = new ArrayList<String>();
						featureValueMap.put( i, featureValue );
						featureNameMap.put( featureFullName, i );
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
			
			String fileTrain = folderName + "/newCategoryFeatures.csv";
			BufferedWriter output = new BufferedWriter ( new OutputStreamWriter ( new FileOutputStream ( new File ( fileTrain )) , "GB2312"), 1024);
			for ( int j =0; j < refinedSelectedFeatureName.size(); j++ ){
				output.write( refinedSelectedFeatureName.get( j ) + ",");
			}
			output.write( "category");
			output.newLine();
			//k��ʾinstance����Ŀ����Ҫ�������ݵ�����
			for ( int k =0; k < featureValueMap.entrySet().iterator().next().getValue().size(); k++ ){
				for ( int j =0; j < refinedSelectedFeatureName.size(); j++ ){	
					if ( featureNameMap.containsKey( refinedSelectedFeatureName.get( j) ) ){
						int index = featureNameMap.get( refinedSelectedFeatureName.get( j ));
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
		    
		    NaiveBayes classify = new NaiveBayes() ;   
		    String[] options = {};
		    classify.setOptions(options);
		 
		    evaluation.crossValidateModel( classify, data, 10, new Random(1));
		    
		    System.out.println ( evaluation.toSummaryString("/nResults/n======/n", true) );
			//System.out.println ( evaluation.toClassDetailsString() );
			//System.out.println ( evaluation.toMatrixString(  ) );
			
		    //Double accuracy = evaluation.weightedFMeasure();
		    Double accuracy = (1.0* (evaluation.truePositiveRate( 0) + evaluation.trueNegativeRate( 0) ) ) / (evaluation.truePositiveRate( 0) + 
		    		evaluation.trueNegativeRate( 0) + evaluation.falsePositiveRate(0) + evaluation.falseNegativeRate(0));
		    //Double accuracy = (1.0* evaluation.truePositiveRate( 0) ) / (evaluation.truePositiveRate( 0) +  evaluation.falsePositiveRate(0) ); precision
		    //Double accuracy = evaluation.fMeasure(1);
		    return accuracy;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}	
	
	public static void main ( String args[] ){
		FeatureCategoryEvaluation evaluation = new FeatureCategoryEvaluation();
		String[] category = { "fChr", "fHst", "cChr", "cHst", "cAnl", "wChr", "wHst", "wCmb"};
		//evaluation.featureSetEvaluation(  "fChr");
		for ( int i =0; i < category.length; i++ ){
			evaluation.featureSetEvaluation(  category[i] );
		}
	}
}
