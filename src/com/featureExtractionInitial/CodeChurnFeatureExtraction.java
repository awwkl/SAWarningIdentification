package com.featureExtractionInitial;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.comon.Constants;
import com.comon.ProjectInfo;
import com.database.DBOperation;

public class CodeChurnFeatureExtraction {
	private DBOperation dbOperation;
	
	public CodeChurnFeatureExtraction ( ){
		dbOperation = new DBOperation();
	}
	
	/*
	 * �Ե�ǰʱ��Ϊ��׼����ȥ��3����(����25��revision)�ı�����
	 * ��Ҫ������ obtainCodeChurnForAllFiles��
	 */
	public HashMap<String, Object> extractCodeChurnInFile_F35_to_F40 (String fileName, HashMap<String, HashMap<String, Object>> codeChurnForFile ) {
		HashMap<String, Object> codeChurn = null;
		
		//String threeMonthBefore = this.obtainThreeMonthBeforeTime();
		if ( codeChurnForFile.containsKey( fileName )){
			codeChurn = codeChurnForFile.get( fileName );
		}
		return codeChurn;
	}
	
	public String obtainThreeMonthBeforeTime ( ){
		SimpleDateFormat dateFormat = new SimpleDateFormat ( "yyyy-MM-dd HH:mm:ss");
		String threeMonthBefore = "";
		try {
			Date nowTime = dateFormat.parse( Constants.CURRENT_COMMIT_TIME );
			
			Calendar calendar = Calendar.getInstance();
			calendar.setTime( nowTime );
			calendar.add( Calendar.MONTH, -3 );
			threeMonthBefore = dateFormat.format( calendar.getTime() );
			//System.out.println( threeMonthBefore );
		
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		return threeMonthBefore;
	}
	
	//25��revision
	public HashMap<String, Object> extractCodeChurnInFile_F126_to_F131 (String fileName,  HashMap<String, HashMap<String, Object>> codeChurnForFile ) {
		HashMap<String, Object> codeChurn = null;
		
		//String twentyFiveRevisionBefore = this.obtainTwentyFiveRevisionBeforeTime();
		if ( codeChurnForFile.containsKey( fileName )){
			codeChurn = codeChurnForFile.get( fileName );
		}
		return codeChurn;
	}
	
	public String obtainTwentyFiveRevisionBeforeTime ( ){
		String sql = "SELECT * FROM " + Constants.COMMIT_INFO_TABLE + " where commitTime < '" + Constants.CURRENT_COMMIT_TIME + 
				"' order by commitTime desc";
		System.out.println( sql );
		ResultSet rs = dbOperation.DBSelect(sql);
		int count = 0;
		String twentyFiveRevisionBefore = "";
		try {
			while ( rs.next() ){
				count++;
				twentyFiveRevisionBefore = rs.getString("commitTime");

				System.out.println("" + count + ": " + twentyFiveRevisionBefore);
				
				if (count == 25) break;
				// if ( count < 25 ){
				// 	count++;
				// }				
				// else if ( count == 25){
				// 	twentyFiveRevisionBefore = rs.getString( "commitTime");
				// }
				// else{
				// 	break;
				// }
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return twentyFiveRevisionBefore;
	}
	
	//ԭ����extractCodeChurnInFile_F35_to_F40 �� extractCodeChurnInFile_F126_to_F131 ֱ�ӵ���obtainCodeChurnInFile����ɿ����ܴ�
	//���ڸĳ�������һ�δ洢����������ֱ�Ӷ�ȡ
	public HashMap<String, HashMap<String, Object>> obtainCodeChurnForAllFiles ( ProjectInfo projectInfo, String type ){
		String priorTime = "";
		if ( type.equals( "revision")){
			priorTime = this.obtainTwentyFiveRevisionBeforeTime();
		}else{
			priorTime = this.obtainThreeMonthBeforeTime();
		}
		
		System.out.println("priorTime: " + priorTime);

		HashMap<String, HashMap<String, Object>> result = new HashMap<String, HashMap<String, Object>>();
		
		for ( int i =0; i < projectInfo.getPackageList().size(); i++ ){
			String packageName = projectInfo.getPackageList().get( i );
			for ( int j =0;  j < projectInfo.getFileListForPackage().get( packageName ).size(); j++ ){
				String fileName = projectInfo.getFileListForPackage().get( packageName).get( j);
				HashMap<String, Object> codeChurn = this.obtainCodeChurnInFile ( fileName, priorTime );
				result.put( fileName, codeChurn );
			}
		}
		return result;
	}
	
	/*
	 * ��extractCodeChurnInFile_F35_to_F40 �� extractCodeChurnInFile_F126_to_F131 ���� 
	 * ǰ���������������ǵõ�������ǰ��ʱ�䣬����25 revision֮ǰ��ʱ��
	 * �����������õõ������code churn��ֵ
	 */
	public HashMap<String, Object> obtainCodeChurnInFile ( String fileName, String priorTime ){
		ArrayList<HashMap<String, Object>> totalCodeChurn = new ArrayList<HashMap<String, Object>>();
		
		try {
			String sql = "SELECT * from " + Constants.COMMIT_CONTENT_TABLE + " where className like '%" + fileName 
					+ "'  and commitTime >= '" + priorTime + "' and commitTime <= '" + Constants.CURRENT_COMMIT_TIME + "'" ;
			System.out.println( sql );
			ResultSet rs = dbOperation.DBSelect(sql);
			
			while ( rs.next() ){
				String commitId = rs.getString( "commitId");
				String commitFileName = Constants.LOG_CODE_FOLDER_OUT + commitId + ".txt";
				HashMap<String, Object> thisCodeChurn = this.obtainCodeChurnBasedCommit(commitFileName, fileName);
				totalCodeChurn.add( thisCodeChurn );
				
				if ( rs.next() )
					continue;
				else
					break;
			}
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
		
		HashMap<String, Object> codeChurnInfo = this.obtainTotalCodeChurn(totalCodeChurn);
		return codeChurnInfo;
	}
	
	
	//��ÿ���ļ���code churn�õ���ȫ����ͣ�Ȼ����ƽ��
	public HashMap<String, Object> obtainTotalCodeChurn ( ArrayList<HashMap<String, Object>> totalCodeChurn ){
		Integer addLine = 0, deleteLine = 0, changeLine = 0, churnLine = 0, growthLine = 0;
		Double percentChurn = 0.0;
		HashMap<String, Object> codeChurnInfo = new HashMap<String, Object>();
		for ( int i = 0; i < totalCodeChurn.size(); i++ ){
			HashMap<String, Object> codeChurnOne = totalCodeChurn.get( i );
			codeChurnInfo = this.addHashMap( codeChurnInfo, codeChurnOne );
			addLine += Integer.parseInt( codeChurnOne.get( "add").toString() );
			deleteLine += Integer.parseInt( codeChurnOne.get( "delete").toString() );
			changeLine += Integer.parseInt( codeChurnOne.get( "change").toString() );
			churnLine += Integer.parseInt( codeChurnOne.get( "churn").toString() );
			growthLine += Integer.parseInt( codeChurnOne.get( "growth").toString() );
			percentChurn += Double.parseDouble( codeChurnOne.get( "percentChurn").toString() );		
		}
		
		codeChurnInfo.put( "add", addLine );
		codeChurnInfo.put( "delete", deleteLine );
		codeChurnInfo.put( "change", changeLine );
		codeChurnInfo.put( "churn", churnLine );
		codeChurnInfo.put( "growth", growthLine );
		
		if ( totalCodeChurn.size() == 0 ){
			percentChurn = 0.0;
		}
		else{
			percentChurn = percentChurn / (1.0*totalCodeChurn.size() ) ;
		}
		codeChurnInfo.put( "percentChurn", percentChurn );
		
		System.out.println( codeChurnInfo.toString() );
		return codeChurnInfo;		
	}
	
	
	public HashMap<String, HashMap<String, Object>> obtainCodeChurnForAllPackages ( ProjectInfo projectInfo, HashMap<String, HashMap<String, Object>> codeChurnFile ){
		HashMap<String, HashMap<String, Object>> result = new HashMap<String, HashMap<String, Object>>();
		
		for ( int i =0; i < projectInfo.getPackageList().size(); i++ ){
			String packageName = projectInfo.getPackageList().get( i );
			ArrayList<String> fileList = projectInfo.getFileListForPackage( ).get( packageName );
			ArrayList<HashMap<String, Object>> totalCodeChurn = new ArrayList<HashMap<String, Object>>();
			
			for ( int j =0; j < fileList.size(); j++ ){
				String fileName = fileList.get( j );
				HashMap<String, Object> codeChurn = new HashMap<String, Object>();
				if ( codeChurnFile.containsKey( fileName ))
					codeChurn = codeChurnFile.get( fileName );
				totalCodeChurn.add( codeChurn );
			}
			
			HashMap<String, Object> codeChurnInfo = this.obtainTotalCodeChurn(totalCodeChurn);
			
			result.put( packageName, codeChurnInfo );
		}
		return result;
	}
	
	
	//��������obtainCodeChurnForAllPackages���õ�����package��codeChurn
	public HashMap<String, Object> extractCodeChurnInPackage_F41_to_F46 ( String packageName, HashMap<String, HashMap<String, Object>> codeChurnForPackage ){
		HashMap<String, Object> codeChurn = null;
		
		//String twentyFiveRevisionBefore = this.obtainTwentyFiveRevisionBeforeTime();
		if ( codeChurnForPackage.containsKey( packageName )){
			codeChurn = codeChurnForPackage.get( packageName );
		}
		return codeChurn;		
	}
	
	public HashMap<String, Object> extractCodeChurnInPackage_F132_to_F137 ( String packageName, HashMap<String, HashMap<String, Object>> codeChurnForPackage ){
		HashMap<String, Object> codeChurn = null;
		
		//String twentyFiveRevisionBefore = this.obtainTwentyFiveRevisionBeforeTime();
		if ( codeChurnForPackage.containsKey( packageName )){
			codeChurn = codeChurnForPackage.get( packageName );
		}
		return codeChurn;	
	}
	
	public HashMap<String, Object> obtainCodeChurnForThisProject ( ProjectInfo projectInfo, HashMap<String, HashMap<String, Object>> codeChurnPackage ){
		ArrayList<HashMap<String, Object>> totalCodeChurn = new ArrayList<HashMap<String, Object>>();
		
		for ( int i =0; i < projectInfo.getPackageList().size(); i++ ){
			String packageName = projectInfo.getPackageList().get( i );
	
			HashMap<String, Object> codeChurn = new HashMap<String, Object>();
			if ( codeChurnPackage.containsKey( packageName ))
				codeChurn = codeChurnPackage.get( packageName );
			totalCodeChurn.add( codeChurn );
		}
		
		HashMap<String, Object> result = this.obtainTotalCodeChurn(totalCodeChurn);
		return result;
	}
	
	//��������obtainCodeChurnForThisProject���õ���project��
	public HashMap<String, Object> extractCodeChurnInProject_F47_to_F52 (  HashMap<String, Object> codeChurnProject){
		return codeChurnProject;		
	}
	
	public HashMap<String, Object> extractCodeChurnInProject_F138_to_F143 ( HashMap<String, Object> codeChurnProject ){
		return codeChurnProject;
	}
	
	//������hashMap�ж�Ӧ��key��ֵ�������
	//���֮���Բ�������Ϊ������percentChurn���ָ�꣬���ʺϵ�������������ƽ������Ӧ��ȫ����������ƽ��
	public HashMap<String, Object> addHashMap ( HashMap<String, Object> map1, HashMap<String, Object> map2){
		HashMap<String, Object> result = new HashMap<String, Object>();
		
		Iterator<Map.Entry<String, Object>> iter = map1.entrySet().iterator();
		while ( iter.hasNext() ){
			Map.Entry<String, Object> entry = iter.next();
			String key = entry.getKey();	
			Object value = entry.getValue();
			
			Object value2 = map2.get( key );			
			
			if ( key.equals( "percentChurn")){
				double newValue = 0.0;
				double dValue = Double.parseDouble( value.toString() );
				double dValue2 = Double.parseDouble( value2.toString() );
				newValue = dValue + dValue2;
			
				Object newObject = new Double(newValue);
				result.put( key , newObject );
			}
			else{
				int newValue = 0;
				int iValue = Integer.parseInt( value.toString() );
				int iValue2 = Integer.parseInt( value2.toString() );
				newValue = iValue + iValue2;
				
				Object newObject = new Integer ( newValue );
				result.put( key, newObject);
			}	
		}
		
		return result;
	}
	
	//��Ҫ�鿴commitFileName���ض��ļ��ı��
	public HashMap<String, Object> obtainCodeChurnBasedCommit ( String commitFileName, String fileName ){
		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader( new File ( commitFileName ) ));
			String line = "";
			int totalCode = 0;
			ArrayList<String> addCodeList = new ArrayList<String>();
			ArrayList<String> deleteCodeList = new ArrayList<String>();
			
			boolean isRelatedFile = false;
			while ( ( line = br.readLine() ) != null ) {
				totalCode ++;
				//fileName��ص��ļ��Ѿ���������
				if ( line.startsWith( "diff") && isRelatedFile == true )
					break;
				//�������Ķ�����fileName�ļ���ص�
				if ( isRelatedFile == false && line.contains( fileName ))
					isRelatedFile = true;
				//����ǲ���صģ�����
				if ( isRelatedFile == false )
					continue;
				
				if ( line.startsWith( "+ ")  ){
					line = line.substring( 2);
					line = line.trim();
					if ( line.equals( ""))
						continue;
					addCodeList.add( line );
				}
				if ( line.startsWith( "- ")){
					line = line.substring( 2);
					line = line.trim();
					if ( line.equals( ""))
						continue;
					deleteCodeList.add( line );
				}			
			}
			
			ArrayList<String> refinedAddCodeList = new ArrayList<String>();
			refinedAddCodeList.addAll( addCodeList );
			
			for ( int i = 0; i < addCodeList.size(); i++ ){
				String addCode = addCodeList.get( i );
				if ( deleteCodeList.contains( addCode )){
					deleteCodeList.remove( addCode );
					refinedAddCodeList.remove( addCode );
				}
			}
			Integer changeLine = addCodeList.size() - refinedAddCodeList.size();
			Integer addLine = refinedAddCodeList.size();
			Integer deleteLine = deleteCodeList.size();
			Integer churnLine = changeLine + addLine + deleteLine;
			Integer growthLine = addLine - deleteLine;
			Double percentChurnedLine = (1.0*churnLine ) / (1.0*totalCode);
			HashMap<String, Object> codeChurnInfo = new HashMap<String, Object>();
			codeChurnInfo.put( "add", addLine );
			codeChurnInfo.put( "delete", deleteLine );
			codeChurnInfo.put( "change", changeLine );
			codeChurnInfo.put( "churn", churnLine );
			codeChurnInfo.put( "growth", growthLine );
			codeChurnInfo.put( "percentChurn", percentChurnedLine );
			
			br.close();
			
			return codeChurnInfo;
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public static void main ( String args[] ){
		CodeChurnFeatureExtraction extraction = new CodeChurnFeatureExtraction();
		
		String fileName = "solr/solrj/src/java/org/apache/solr/client/solrj/impl/CloudSolrClient.java";
		
		//extraction.extractCodeChurnInFile_F35_to_F40(fileName);
	}
}
