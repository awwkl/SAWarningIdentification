package com.featureExtractionInitial;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import com.comon.Constants;
import com.comon.DateTimeTool;
import com.comon.StaticWarning;
import com.database.DBOperation;

public class WarningHistoryFeatureExtraction {
	private DBOperation dbOperation;
	
	public WarningHistoryFeatureExtraction ( ){
		dbOperation = new DBOperation();
	}
	
	
	/*
	 * ��openRevision���ƣ� ���codeInfo���ж��д��룬�������50%���ϴ��붼ɾ���ĵ�һ��revisionNumber
	 * 
	 * �����ҵ����ļ���currentRevision֮����Щcommit�з����˱����Ȼ����commit log�н���ƥ��
	 * deletion��open�߼������ӵĵط�����:
	 * ��ʱ�� - XXXX, ���������� + XXXX.
	 * Ҳ����˵ĳ��䣬��Ȼɾ�������������һģһ������䡣���������Ҫ�ų�
	 * 
	 * type ��Ϊ "bug fix", "non bug fix", "all"
	 * �ֱ��ʾֻ��bug fix change�����ң�ֻ��non bug fix�����ң��Լ������е�������
	 * 
	 * ע��������
	 * �ú���ͬʱ������ĳwarning���޸�ǰ�����ļ���ɾ���������Ӧ���ȵ��øú������õ���Щwarning���޸�ǰ�ļ���ɾ���ˣ����������ȥ��
	 * 
	 * ���revisionNumber = -1�����ʾ��û��close
	 */
	public HashMap<String, Object> obtainAlertCloseTimeRevision ( int index, StaticWarning warning, String type, String closeEndTime ){
		String fileName = warning.getBugLocationList().get(0).getClassName();
		ArrayList<String> codeInfo = new ArrayList<String>();
		
		for ( int j = 0;  j < warning.getBugLocationList().size(); j ++){
			codeInfo.addAll( warning.getBugLocationList().get(j).getCodeInfoList()  );
		}
		
		String typeSql = "";
		if ( type.equals( "bug fix"))
			typeSql = "and issueType = 'BUG'";
		if ( type.equals( "non bug fix"))
			typeSql = "and issueType != 'BUG'";
			
		if ( closeEndTime.equals( ""))
			closeEndTime = Constants.CURRENT_TIME;
		
		String sql = "SELECT * from " + Constants.COMMIT_CONTENT_TABLE + " where className like '%" + fileName 
				+ "'  and commitTime > '" + Constants.CURRENT_COMMIT_TIME + "' and commitTime <= '" + closeEndTime + "' "
				+ typeSql + " order by commitTime ";
		
		System.out.println( sql );
		ResultSet rs = dbOperation.DBSelect(sql);
	
		int revisionNumber = -1;
		String commitTime = "";
		boolean isDeletion = false;
		try {
			while ( rs.next() ){
				String commitId = rs.getString( "commitId");
				commitTime = rs.getString( "commitTime");
				
				//���ܴ���ɾ����file��������������������ǳ��������ܻ��ѵ������ȥ��
				String commitType = rs.getString( "commitType");
				if ( commitType.equals( "D")){
					revisionNumber = Integer.parseInt( commitId );
					isDeletion = true;
					break;
				}
				
				BufferedReader br = new BufferedReader(new FileReader( new File ( Constants.LOG_CODE_FOLDER_OUT + commitId + ".txt" ) ));
				String line = "";
				boolean isRelatedFile = false;
				ArrayList<String> addCodeList = new ArrayList<String>();
				ArrayList<String> deleteCodeList = new ArrayList<String>();
				
				ArrayList<String> writeCodeList = new ArrayList<String>();
				
				while ( ( line = br.readLine() ) != null ) {
					if ( line.startsWith( "diff")) {
						if ( isRelatedFile ) 
							break;
						if ( line.contains(fileName) ) 
							isRelatedFile = true;
					} 
					if ( isRelatedFile == false )
						continue;
					
					writeCodeList.add( line );
					if ( line.startsWith( "+") && line.length() > 1 && !line.startsWith("+++") ){
						line = line.substring( 1);
						line = line.trim();
						if ( line.equals( ""))
							continue;
						addCodeList.add( line );
					}
					if ( line.startsWith( "-") && line.length() > 1 && !line.startsWith("---") ){
						line = line.substring( 1);
						line = line.trim();
						if ( line.equals( ""))
							continue;
						deleteCodeList.add( line );
					}
				}
				
				//��addCodeList��deleteCodeList�е��ļ����жԱȣ���ͬ����ȥ��
				ArrayList<String> refinedDeleteCodeList = new ArrayList<String>();
				for ( int i =0; i < deleteCodeList.size(); i++  ){
					String deleteCode = deleteCodeList.get( i );
					if ( addCodeList.contains( deleteCode ))
						continue;
					refinedDeleteCodeList.add( deleteCode );
				}
				
				int equalTimes = 0;
				for ( int i = 0; i < codeInfo.size(); i++ ){
					String codeQuery = codeInfo.get(i).trim();
					if ( refinedDeleteCodeList.contains( codeQuery )){
						equalTimes ++;
					}
				}
				
				if ( equalTimes >= 1 ){
					BufferedWriter output = new BufferedWriter ( new OutputStreamWriter ( new FileOutputStream ( new File ( Constants.GROUND_TRUTH_FOLDER + index +".txt" )) , "GB2312"), 1024);
					output.write( "category: " + warning.getBugInfo().getCategory() + "\r\n");
					output.write( "type: " + warning.getBugInfo().getType() + "\r\n");
					output.write( "class name: " + warning.getBugLocationList().get(0).getClassName() + "\r\n");
					for ( int i =0; i < warning.getBugLocationList().size(); i++ ){
						output.write( i + " method name: " + warning.getBugLocationList().get(i).getRelatedMethodName() + "\r\n");
						output.write( "start line: " + warning.getBugLocationList().get(i).getStartLine() + "\r\n" );
						output.write( "end line: " + warning.getBugLocationList().get(i).getEndLine() + "\r\n" );
						for ( int j =0; j < warning.getBugLocationList().get(i).getCodeInfoList().size(); j++ )
							output.write( "code: " + warning.getBugLocationList().get(i).getCodeInfoList().get(j) + "\r\n" );
						output.newLine();
					}
					
					output.write( "=======================================================================");
					output.newLine();
					
					if ( isDeletion == true ){
						output.write( "is delete ?  yes!" );
					}else{
						output.write( "is delete ?  no!");
					}
					output.newLine();
					output.write( "=======================================================================");
					output.newLine();
					
					for ( int i =0; i < writeCodeList.size(); i++ ){
						output.write( writeCodeList.get( i ));
						output.newLine();
					}
					output.flush();
					output.close();
				}
				
				//1�д��룬1��ƥ����˳���2�д��룬1��ƥ����˳�
				if ( equalTimes >= (1+ codeInfo.size()) / 2 ){
				//if ( equalTimes >= 1 ){
					revisionNumber = Integer.parseInt( commitId );
					break;
				}							
			
				if ( revisionNumber != -1 )
					break;
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		HashMap<String, Object> result = new HashMap<String, Object>();
		result.put( "revision", revisionNumber );
		result.put( "time", commitTime );
		result.put( "isDeletion", isDeletion );
		
		System.out.println( revisionNumber + " " + commitTime + " " + isDeletion );
		return result;
	}
	
	/*
	 * �Ҵ��codeInfo�����е�һ�γ��ֵ�revisionNumber
	 * ���codeInfo���ж��д��룬�������50%���ϴ��붼���ֵĵ�һ��revisionNumber
	 * 
	 * �����ҵ����ļ�����Щcommit�з����˱����Ȼ����commit log�н���ƥ��
	 */
	public HashMap<String, Object>  obtainAlertOpenRevision( String fileName, ArrayList<String> codeInfo ){
		String sql = "SELECT * from " + Constants.COMMIT_CONTENT_TABLE + " where className like '%" + fileName 
				+ "'  and commitTime <= '" + Constants.CURRENT_COMMIT_TIME + "' order by commitTime ";
		System.out.println( sql );
		ResultSet rs = dbOperation.DBSelect(sql);
		
		int revisionNumber = -1;
		int equalTimes = 0;
		String commitTime = "";
		try {
			while ( rs.next() ){
				String commitId = rs.getString( "commitId");
				commitTime = rs.getString( "commitTime");
				
				BufferedReader br = new BufferedReader(new FileReader( new File ( Constants.LOG_CODE_FOLDER_OUT + commitId + ".txt" ) ));
				String line = "";
				boolean isRelatedFile = false;
				while ( ( line = br.readLine() ) != null ) {
					if ( line.startsWith( "diff")) {
						if ( isRelatedFile ) 
							break;
						if ( line.contains(fileName) ) 
							isRelatedFile = true;
					} 
					if ( isRelatedFile == false )
						continue;
					
					if ( line.startsWith( "+") && line.length() > 1 && !line.startsWith("+++") ){
						line = line.substring(1);
						line = line.trim();
						if ( line.equals( ""))
							continue;
						
						for ( int i = 0; i < codeInfo.size(); i++ ){
							if ( line.equals( codeInfo.get(i).trim() )){
								equalTimes ++;
							}
						}
						//1�д��룬1��ƥ����˳���2�д��룬1��ƥ����˳�
						if ( equalTimes >= (1+ codeInfo.size()) / 2 ){
							revisionNumber = Integer.parseInt( commitId );
							break;
						}							
					}
				}
				if ( revisionNumber != -1 )
					break;
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if ( revisionNumber == -1 ){
			System.out.println( "could not find the alert open revision!");
			revisionNumber = Constants.GIVEN_EARLIEST_REVISION_NUMBER;
			commitTime = Constants.GIVEN_EARLIEST_TIME;
		}
			
		HashMap<String, Object> result = new HashMap<String, Object>();
		result.put( "revision", revisionNumber );
		result.put( "time", commitTime);
		
		System.out.println ( revisionNumber  + " " + commitTime );
		return result;
	}
	
	//������obtainAlertOpenRevisionForAllFiles���õ�����warning��openRevisionTimeList
	public Integer extractAlertOpenRevisionTime_F70 ( int index, HashMap<String, HashMap<Integer, Object>> openRevisionTimeList ){
		int revision = -1;
		if ( openRevisionTimeList.get("revision").containsKey( index ))
			revision = (int) openRevisionTimeList.get("revision").get( index );
		
		return revision;
	}
	
	public HashMap<String, HashMap<Integer, Object>> obtainAlertOpenRevisionForAllFiles ( ArrayList<StaticWarning> warningList ){
		HashMap<String, HashMap<Integer, Object>> result = new HashMap<String, HashMap<Integer, Object>>();
		//key Ϊ warning�� index
		HashMap<Integer, Object> revisionResult = new HashMap<Integer, Object>();
		HashMap<Integer, Object> timeResult = new HashMap<Integer, Object>();
		
		for ( int i =0; i < warningList.size(); i++ ){
			StaticWarning warning = warningList.get( i );
			String fileName = warning.getBugLocationList().get(0).getClassName();
			
			ArrayList<String> codeInfo = new ArrayList<String>();
			for ( int j = 0; j < warning.getBugLocationList().size(); j++ ){
				ArrayList<String> temp = warning.getBugLocationList().get(j).getCodeInfoList();
				codeInfo.addAll( temp );
			}		
				
			HashMap<String, Object> resultInFile = this.obtainAlertOpenRevision( fileName, codeInfo);
			
			revisionResult.put( i, resultInFile.get( "revision"));
			timeResult.put( i, resultInFile.get( "time" ));
		}
		result.put( "revision", revisionResult );
		result.put( "time", timeResult );
		
		return result;
	}
	
	//��CodeHistoryExtraction�е��ã�ֻ����һ�Σ��õ�����file�� openRevisionNumber
	public HashMap<Integer, Integer> obtainAlertOpenRevisionNumberForAllFiles ( ArrayList<StaticWarning> warningList ){
		HashMap<Integer, Integer> openRevisionNumber = new HashMap<Integer, Integer>();
		for ( int i =0; i < warningList.size(); i++ ){
			StaticWarning warning = warningList.get( i );
			String fileName = warning.getBugLocationList().get(0).getClassName();
			
			ArrayList<String> codeInfo = new ArrayList<String>();
			for ( int j = 0; j < warning.getBugLocationList().size(); j++ ){
				ArrayList<String> temp = warning.getBugLocationList().get(j).getCodeInfoList();
				codeInfo.addAll( temp );
			}		
				
			HashMap<String, Object> result = this.obtainAlertOpenRevision(fileName, codeInfo);
			int revision = (int) result.get( "revision");
			openRevisionNumber.put( i , revision );
		}
		return openRevisionNumber;
	}
	
	
	public Integer extractAlertModification_F61 ( StaticWarning warning, int index, HashMap<String, HashMap<Integer, Object>> openRevisionTimeList ){
		int modifyNum = 0;
		
		String fileName = warning.getBugLocationList().get(0).getClassName();
		String openTime = Constants.CURRENT_COMMIT_TIME;
		if ( openRevisionTimeList.get( "time" ).containsKey( index ) ) 
			openTime = (String) openRevisionTimeList.get( "time" ).get( index );
		
		String sql = "SELECT count(*) from " + Constants.COMMIT_CONTENT_TABLE + " where className like '%" + fileName 
				+ "'  and commitTime <= '" + Constants.CURRENT_COMMIT_TIME + "' and commitTime > '" + openTime + "'";
		System.out.println( sql );
		ResultSet rs = dbOperation.DBSelect(sql);
		
		try {
			if ( rs.next() ){
				modifyNum = Integer.parseInt( rs.getString( 1 ));
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return modifyNum;
	}
	
	public Integer extractAlertLifeRevision_F77 ( int index , HashMap<String, HashMap<Integer, Object>> openRevisionTimeList ){
		Integer openRevision = -1;
		if ( openRevisionTimeList.get( "revision" ).containsKey( index ) ) 
			openRevision = (Integer) openRevisionTimeList.get( "revision" ).get( index );
		
		Integer currentRevisin = Constants.CURRENT_REVISION_NUMBER;
		int lifeRevision = openRevision - currentRevisin ;
		
		return lifeRevision;
	}
	
	public Integer extractAlertLifeTime_F88 ( int index,  HashMap<String, HashMap<Integer, Object>> openRevisionTimeList ){
		String openTime = Constants.CURRENT_COMMIT_TIME;
		if ( openRevisionTimeList.get( "time" ).containsKey( index ) ) 
			openTime = (String) openRevisionTimeList.get( "time" ).get( index );
		 
		String currentTime = Constants.CURRENT_COMMIT_TIME;	
		int dayGap = DateTimeTool.obtainDayGap( openTime, currentTime );
		
		return dayGap;
	}
	
	
	//���ڸ�revision���е�warning��ͳ�����Ƿ�closed��fixed����Ҳ����˵�Ƿ�Ϊ������bug
	public ArrayList<String> obtainWarningStatus ( ArrayList<StaticWarning> warningList, String type ){
		ArrayList<String> warningStatusList = new ArrayList<String>();
		
		for ( int i = 0;  i < warningList.size(); i++ ){
			StaticWarning warning = warningList.get( i );
			String fileName = warning.getBugLocationList().get(0).getClassName();
			ArrayList<String> codeInfo = new ArrayList<String>();
			
			for ( int j = 0;  j < warning.getBugLocationList().size(); j ++){
				codeInfo.addAll( warning.getBugLocationList().get(j).getCodeInfoList()  );
			}
			
			HashMap<String, Object> status = this.obtainAlertCloseTimeRevision(i,  warning, type, "");
			if ( (int)status.get("revision") == -1 ){
				warningStatusList.add( "open");
			}else if ( (boolean)status.get("isDeletion") == true ){
				System.out.println ( "the file is deleted!");
				warningStatusList.add( "deleted");
			}else{
				warningStatusList.add( "close");
			}
		}
		
		return warningStatusList;
	}	
		
	public static void main ( String args[] ){
		WarningHistoryFeatureExtraction extraction = new WarningHistoryFeatureExtraction();
		
		ArrayList<String> codeInfo = new ArrayList<String>();
		codeInfo.add( "private static final boolean VERBOSE = false;");
		codeInfo.add( "private final static class TermsWriter extends TermsConsumer {");
		codeInfo.add( "if (VERBOSE) System.out.println(\"    startDoc docID=\" + docID + \" freq=\" + termDocFreq);");
		codeInfo.add( "assert docID == 0 || delta > 0;");
		codeInfo.add( "lastPos = pos;");
		
		String fileName = "lucene/core/src/java/org/apache/lucene/codecs/memory/MemoryPostingsFormat.java";
		
		//extraction.obtainAlertCloseTimeRevision( fileName, codeInfo, "all", "");
		
	}
	
}
