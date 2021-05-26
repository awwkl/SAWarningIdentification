package com.git;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.comon.Constants;
import com.database.DBOperation;


/*
 * ��git log�õ��������ļ�log��logCode���н�����
 * �õ��Ľ���洢�����ݿ���������У�commit_info��commit_content
 * 
 * ������ó���󣬻�������DatabaseReOrganization���������������������֯
 */
public class LogParser {
	private DBOperation dbOperation;
	
	public LogParser ( ){
		dbOperation = new DBOperation();
	}
	
	public void parseLogInfo ( ){
		
		int fieldNumber = 20;
		int issueNameLength = 100;

		try {
			BufferedReader br = new BufferedReader(new FileReader( new File ( Constants.LOG_FILE_IN )));
			BufferedReader brCode = new BufferedReader(new FileReader( new File ( Constants.LOG_CODE_FILE_IN )));
			
			String line = "";
			ArrayList<String> commitFileList = new ArrayList<String>();
			ArrayList<String> commitFileTypeList = new ArrayList<String>();
			
			Map<String, Integer> hashIdToCommitId = new HashMap<String, Integer>();
			int commitId = 0;
			int contentId = 0;
			String time = "";
			int issueIdNumber = 0;
			
			while ( ( line = br.readLine() ) != null ) {
				if ( line.startsWith( "GitCommitStart")){
					int column = 0;
					
					//System.out.println ( line );
					
					Pattern pattern = Pattern.compile("[|#&]+");
					String[] temp= pattern.split( line );
					//System.out.println ( temp.length );
					/*
					for ( int i =0; i < temp.length; i++ ){
						System.out.println ( i + " : " + temp[i] );
					}
					*/
					
					String commitHash = temp[0].substring(new String ("GitCommitStart:").length()).trim();
					String email = temp[2].trim();
					
					time = temp[3].trim();
					time = this.transferDateTime( time );
					
					String commitMessage = "";
					for ( int i = 5; i < temp.length ; i++ ){
						commitMessage += temp[i] + " ";
					}
					commitMessage = commitMessage.trim();
					System.out.println (  commitMessage );
					int index = commitMessage.indexOf( " ");
					
					String issueId =  "0";
					String issueName = commitMessage;
					//���lucen+solr��Ŀ��
					if ( (commitMessage.startsWith( "SOLR-") || commitMessage.startsWith( "LUCENE-")  ) && index > 0 ){
						issueId = commitMessage.substring( 0, index).trim();
						issueId = issueId.replaceAll("[\\pP\\p{Punct}]", "");   //�����"SOLR-7653;"�ܶ��ַ�
						issueId = issueId.replace( "SOLR", "SOLR-");
						issueId = issueId.replace( "LUCENE", "LUCENE-");
						
						issueName = commitMessage.substring( index+1 ).trim();			
						issueIdNumber++;
						//System.out.println ( issueId  );
					}
					//���maven��Ŀ��,����derby��Ŀ
					if ( commitMessage.contains( "MNG") || commitMessage.contains( "DERBY") || commitMessage.contains( "LANG")){
						Pattern patternMvn = Pattern.compile(  "[a-zA-Z]+-\\d+");
						Matcher matcher = patternMvn.matcher( commitMessage );
						if ( matcher.find() ){
							issueId =  matcher.group(0);
							issueName = commitMessage;
							issueIdNumber ++;
						}	
					}
					
							
					//дcommitInfo��	
					issueName = issueName.replaceAll( "'", " ");
					
					//дcommitContent��
					if ( commitFileList.size() != 0 ){
						//�����ǵ���һ��commitInfo�������õ�����һ����commitId
						for ( int i = 0; i< commitFileList.size() ; i++ ){
							this.storeToCommitContent(contentId, commitFileList.get( i ), commitFileTypeList.get( i ), commitId, time );
							contentId++;
						}
						
						commitFileList = new ArrayList<String>();
					}				
					
					commitId++;
					
					this.storeToCommitInfo(commitId, commitHash.trim(), time, issueId, issueName, "task", email);
					hashIdToCommitId.put( commitHash.trim(),  commitId );
				}
				if ( line.startsWith( "A") || line.startsWith( "M") || line.startsWith( "D")){
					String commitFile = line.substring( 1 ).trim();
					commitFileList.add( commitFile);	
					commitFileTypeList.add( line.substring( 0, 1).trim() );
				}
			}
			
			//�����һ��
			//дcommitContent��
			if ( commitFileList.size() != 0 ){
				//�����ǵ���һ��commitInfo�������õ�����һ����commitId
				for ( int i = 0; i< commitFileList.size() ; i++ ){
					this.storeToCommitContent(contentId, commitFileList.get( i ), commitFileTypeList.get( i ), commitId, time );
					contentId++;
				}
			}		
			
			System.out.println ( "commitId: " + commitId );
			System.out.println ( "issueNumber: " + issueIdNumber );
			br.close();
			
			BufferedWriter output = new BufferedWriter ( new OutputStreamWriter ( System.out ) );
			while ( ( line = brCode.readLine() ) != null) {
				if ( line.startsWith( "GitDiffStart:") ){
					output.flush(); 
					output.close();
					
					Pattern pattern = Pattern.compile("[|#&]+");
					String[] temp= pattern.split( line );
					String commitHash = temp[0].substring(new String ("GitDiffStart:").length()).trim();
					commitId = -1;
					if ( hashIdToCommitId.containsKey( commitHash.trim() )){
						commitId = hashIdToCommitId.get( commitHash.trim() );
					} else{
						System.out.println ( "do not have the hashid : " + commitHash.trim() );
					}

					output = new BufferedWriter ( new OutputStreamWriter ( new FileOutputStream ( new File ( Constants.LOG_CODE_FOLDER_OUT + commitId + ".txt" )) , "GB2312"), 1024);
				}
				output.write(line);
				output.newLine();
			}
			output.flush();
			output.close();
			
			brCode.close();
			
			dbOperation.DBClose();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}
	
	/*
	 * Tue Dec 6 13:11:36 2016 -0500
	 * ת��Ϊ  'YYYY-MM-DD HH:MM:SS'
	 */
	public String transferDateTime ( String dateTime ){
		//System.out.println( dateTime );
		String[] temp = dateTime.split( " ");
		String month = temp[1];
		if ( month.equals( "Jan"))
			month = "01";
		else if ( month.equals( "Feb"))
			month = "02";
		else if ( month.equals( "Mar"))
			month = "03";
		else if ( month.equals( "Apr"))
			month = "04";
		else if ( month.equals( "May"))
			month = "05";
		else if ( month.equals( "Jun"))
			month = "06";
		else if ( month.equals( "Jul"))
			month = "07";
		else if ( month.equals( "Aug"))
			month = "08";
		else if ( month.equals( "Sep"))
			month = "09";
		else if ( month.equals( "Oct"))
			month = "10";
		else if ( month.equals( "Nov"))
			month = "11";
		else if ( month.equals( "Dec"))
			month = "12";
		
		String day = temp[2];
		int dayInt = Integer.parseInt( day );
		if ( dayInt < 10 )
			day = "0" + day;
		
		String sqlDateTime = temp[4] + "-" + month + "-" + day + " " + temp[3];
		return sqlDateTime;
	}
	
	public void storeToCommitInfo( int commitAutoId, String commitHashId, String commitTime, String issueId, String issueName, String issueType, String email  ){
		String sql = "insert into " + Constants.COMMIT_INFO_TABLE + " values ( " + commitAutoId + ", '" +
			commitHashId + "', '" + commitTime +  "', '" + issueId + "', '"  + issueName + "', '"  + issueType + "', '"
					+ email + "' )" ; 
		
		System.out.println ( sql );
		dbOperation.DBUpdate(sql);	
	}
	
	
	public void storeToCommitContent ( int contentId, String className, String commitType, int commitId, String commitTime  ){
		String sql = "insert into " + Constants.COMMIT_CONTENT_TABLE + " values ( " + contentId + ", '" +
				className + "', '" + commitType + "'," + commitId + ", '" + commitTime + "', 'task' ) "; 
				
		System.out.println ( sql );
		dbOperation.DBUpdate(sql);	
	}
	
	
	public static void main ( String args[] ){
		File output_dir = new File (Constants.LOG_CODE_FOLDER_OUT);
		output_dir.mkdirs();	// make directory if not exists
		
		LogParser parser = new LogParser();
		parser.parseLogInfo();
		
		/*
		String code = "[MNG-2199] Support version ranges in parent";
		Pattern pattern = Pattern.compile(  "[a-zA-Z]+-\\d+");
		Matcher matcher = pattern.matcher( code );
		if ( matcher.find() ){
			System.out.println( matcher.group(0));
		}	
		*/	
	}
}
