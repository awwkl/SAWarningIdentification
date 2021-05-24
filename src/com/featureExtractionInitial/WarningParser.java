package com.featureExtractionInitial;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import com.comon.BugInfo;
import com.comon.BugLocation;
import com.comon.Constants;
import com.comon.StaticWarning;
import com.comon.StaticWarningInfo;
import com.comon.Constants.BUG_LOCATION_REGION_TYPE;

/*
 * ��fingBugs���ɵ�xml�ļ��У�ʶ�����bug��Ӧ��class �� codeLine
 */
public class WarningParser {
	
	public ArrayList<StaticWarning> parseFingbugsWarnings (String fileName  ){
		ArrayList<StaticWarning> warningInfoList = new ArrayList<StaticWarning>();
				
		SAXBuilder builder = new SAXBuilder();
		try {
			Document doc = builder.build( fileName );
			Element rootElement = doc.getRootElement();
			List warningList = rootElement.getChildren( "BugInstance");
			
			System.out.println( warningList.size() );
			for ( int i = 0;  i < warningList.size(); i++ ){
				System.out.println("--- BugInstance " + i + " ---");
				Element element = (Element) warningList.get( i );
				
				String category = element.getAttribute( "category").getValue();
				//ֻ����ĳЩ�����warning
				/*
				int p = 0;
				for ( ; p < Constants.WARN_CATEGORY_UNDER_INVESTIGATION.length; p++ ){
					if ( category.equals( Constants.WARN_CATEGORY_UNDER_INVESTIGATION[p]))
						break;
				}
				if ( p == Constants.WARN_CATEGORY_UNDER_INVESTIGATION.length )
					continue;
				*/
				
				String type = element.getAttribute( "type").getValue();
				Integer priority = Integer.parseInt( element.getAttribute( "priority").getValue() );
				Integer rank = Integer.parseInt( element.getAttribute( "rank").getValue() );
				
				BugInfo bugInfo = new BugInfo ( type, priority, rank ,category );			
						
				ArrayList<BugLocation> bugLocationList = new ArrayList<BugLocation>();
				//���Ǵӵ�����sourceLine�еõ�����Ϣ���ȶ����������������ȷ���Ƿ���Ҫ��Ϊ���յ�
				ArrayList<BugLocation> secBugLocationList = new ArrayList<BugLocation>();     
				
				//����е�����sourceLine�����õ�����sourceLine������	
				List codeList = element.getChildren( "SourceLine");
				if ( codeList != null && codeList.size() != 0 ){
					for ( int j = 0; j< codeList.size(); j++ ){
						Element detailElement = (Element) codeList.get( j );
						if ( detailElement.getAttribute( "sourcepath") == null || detailElement.getAttribute( "start") == null)
							continue;
						
						String className = detailElement.getAttribute( "sourcepath").getValue();
						Attribute attributeStart = detailElement.getAttribute("start");
						Integer startCodeLine = Integer.parseInt( attributeStart.getValue() );
						Attribute attributeEnd = detailElement.getAttribute( "end");
						Integer endCodeLine = Integer.parseInt( attributeEnd.getValue() );
						
						BugLocation bugLocation = new BugLocation ( className, startCodeLine, endCodeLine, BUG_LOCATION_REGION_TYPE.DEFAULT, "" );
						
						//��ʱ������������sourceLine���ų�����ȵ������
						boolean tag = true;
						for ( int k = 0; k < bugLocationList.size() && tag ; k++ ) {
							if ( bugLocationList.get(k).getStartLine().equals( startCodeLine ) && bugLocationList.get(k).getEndLine().equals( endCodeLine )
									&& bugLocationList.get(k).getClassName().equals(className) )
								tag = false;
						}
						if ( (bugLocation.getStartLine() != 1 ) && ( bugLocationList.size() == 0  || tag == true ) ){
							bugLocationList.add( bugLocation );
						}						
					}
				}
				
				String[] regionStr = {"Class", "Field", "Method", "Type" };
				BUG_LOCATION_REGION_TYPE[] region = {BUG_LOCATION_REGION_TYPE.CLASS, BUG_LOCATION_REGION_TYPE.FIELD, 
						BUG_LOCATION_REGION_TYPE.METHOD, BUG_LOCATION_REGION_TYPE.TYPE };
				for ( int j = 0; j< regionStr.length ; j++ ){
					ArrayList<BugLocation> bugLocationListPerRegion = this.parseSpecificSite(element, regionStr[j], region[j] );
					secBugLocationList.addAll( bugLocationListPerRegion );
				}	 
				
				
				//ֻ����bugLocationList�еģ�����Ҫ����secBugLocationList�еģ�����Ҫ��secBugLocationList�е���Ϣ��bugLocationList�е���Ϣ�����޸�
				if ( bugLocationList.size() != 0 ){
					String location = bugLocationList.get(0).getClassName();
					int j = 1;
					//�ж��Ƿ����е�fileName��ͬһ����������ǣ���Ҫ��warning
					for ( ; j < bugLocationList.size(); j++ ){
						String newLocation = bugLocationList.get( j ).getClassName();
						if ( !location.equals( newLocation ) ) 
							break;
					}
					if ( j < bugLocationList.size() ){
						System.out.println ( "@SKIP: Multiple class name! Do not store the warning!");
					}
					else{
						String methodName = "";
						for ( j =0;  j < secBugLocationList.size(); j++ ){
							BugLocation bugLoc = secBugLocationList.get( j );
							if ( bugLoc.getRegion() == Constants.BUG_LOCATION_REGION_TYPE.METHOD 
									&& bugLoc.getClassName().equals( bugLocationList.get(0).getClassName() ) ) {
								methodName = bugLoc.getRelatedMethodName();
							}
						}
						
						if ( !methodName.equals( "")){
							for ( j = 0; j < bugLocationList.size(); j++ ){
								bugLocationList.get(j).setRelatedMethodName( methodName );
							}
						}
						
						StaticWarning warning = new StaticWarning ( bugInfo, bugLocationList );
						warningInfoList.add( warning );
						System.out.println( "@ADD primary: " + warning.toString() );
					}			
				}
				//����û�е�����SourceLine�������ֱ�Ӱ����еĶ�����
				else if ( secBugLocationList.size() != 0 ){
					StaticWarning warning = new StaticWarning ( bugInfo, secBugLocationList );
					warningInfoList.add( warning );
					System.out.println( "@ADD secondary: " + warning.toString() );
				}				
				else{
					System.out.println ( "@SKIP: Wrong parse! Do not have the information!" + bugInfo.getType() );
				}
			}
			System.out.println( warningInfoList.size() );
		} catch (JDOMException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
		
		//warningInfoList = this.obtainCodeInfo(warningInfoList, folderName);
		return warningInfoList;
	}
	
	public ArrayList<BugLocation> parseSpecificSite( Element element, String regionStr, BUG_LOCATION_REGION_TYPE region ){
		ArrayList<BugLocation> bugLocationList = new ArrayList<BugLocation>();
		
		List typeList = element.getChildren( regionStr );	// element.getChildren("Class")
		if ( typeList != null && typeList.size() != 0  ){
			for ( int j = 0; j < typeList.size(); j++ ){	// for each sub-element
				Element detailElement = (Element) typeList.get( j );
				
				String methodName = "";
				if ( region == Constants.BUG_LOCATION_REGION_TYPE.METHOD ){
					Attribute attributeName = detailElement.getAttribute( "name");
					if ( attributeName != null )
						methodName = attributeName.getValue();
				}
				
				BugLocation bugLocation = this.generateBugLocation(detailElement,  region);
				if ( bugLocation != null ){
					bugLocation.setRelatedMethodName( methodName );
					bugLocationList.add( bugLocation );
				}					
			}
		}
		
		return bugLocationList;
	}
	
	public BugLocation generateBugLocation ( Element element, BUG_LOCATION_REGION_TYPE region ){
		String className = "";
		Integer startCodeLine = 0;
		Integer endCodeLine = 0;
		
		List detailList = element.getChildren( "SourceLine");
		//Ĭ��ֻ��һ��
		if ( detailList.size() == 0 )
			return null;
		
		Element detailElement = (Element) detailList.get( 0 );
		
		Attribute attribute = detailElement.getAttribute( "sourcepath");
		if ( attribute != null )
			className = attribute.getValue();
		Attribute attributeStart = detailElement.getAttribute("start");
		if ( attributeStart != null )
			startCodeLine = Integer.parseInt( attributeStart.getValue() );
		Attribute attributeEnd = detailElement.getAttribute( "end");
		if ( attributeEnd != null )
			endCodeLine = Integer.parseInt( attributeEnd.getValue() );
		
		if ( className.equals( "") || startCodeLine == 0 || endCodeLine == 0 )
			return null;
		
		BugLocation bugLocation = new BugLocation ( className, startCodeLine, endCodeLine, region, "" );
		return bugLocation;
	}
	
	
	public ArrayList<String> obtainAllFiles ( String filePath, String relativePath ){	// obtainAllFiles(Constants.FOLDER_NAME, Constants.FOLDER_NAME)
		ArrayList<String> fileList = new ArrayList<String>();
		
		File root = new File ( filePath );
		File[] files = root.listFiles();
		for ( File file: files ){
			if ( file.isDirectory() ){
				ArrayList<String> fileListFolder = obtainAllFiles ( file.getAbsolutePath(), relativePath );
				fileList.addAll( fileListFolder );
			}
			else{
				String absolutePath = file.getAbsolutePath();
				String newRelativePath = relativePath.replace( "//" , "\\");
				String path = absolutePath.substring( newRelativePath.length() );
				fileList.add( path );
				//System.out.println ( path );
			}
		}
		return fileList;
	}
	
	/*
	 * ���иú�����ͨ��codeLine�� Դ������Ϣ���õ�codeInfo
	 */
	public ArrayList<StaticWarning> obtainCodeInfo( ArrayList<StaticWarning> warningInfoList, String folderName  ){	// obtainCodeInfo(warningList, Constants.FOLDER_NAME)
		
		ArrayList<String> fileList = this.obtainAllFiles( folderName, folderName );

		for ( int i = 0; i < warningInfoList.size(); i++ ){
			StaticWarning warning = warningInfoList.get( i );
			ArrayList<BugLocation> bugLocationList = warning.getBugLocationList();
			
			for ( int j = 0; j < bugLocationList.size(); j++ ){
				BugLocation location = bugLocationList.get(j);
				String className = location.getClassName();
				className = className.replace( "/", "\\");
				System.out.println("[Bug " + i + "] Class name: " + className);

				Integer startCodeLine = location.getStartLine();
				Integer endCodeLine = location.getEndLine();
				BUG_LOCATION_REGION_TYPE region = location.getRegion();
				
				ArrayList<String> codeInfoList = new ArrayList<String>();
				boolean isFound = false;
				for (String filePath : fileList) {
					if (filePath.contains(className)) {
						className = className.replace( "\\", "//");
						try (BufferedReader br = new BufferedReader(new FileReader( new File ( folderName + filePath )))) {
							System.out.println ( "	File found: " + folderName + filePath);
							
							String line = "";
							int codeIndex = 0;
							while ( ( line = br.readLine() ) != null ) {
								codeIndex += 1;
								if ( codeIndex >= startCodeLine && codeIndex <= endCodeLine ){
									codeInfoList.add( line );
								}
							}	
							location.setCodeInfoList( codeInfoList );
						} catch (FileNotFoundException e) {
							e.printStackTrace();
						} catch (IOException e) {
							e.printStackTrace();
						}

						isFound = true;
						break;
					}
				}

				if (!isFound)
					System.out.println ( "	@@@ Class not found: " + className );
			}
		}	
		return warningInfoList;
	}
	
	//��warningInfoList��û�д�����Ϣ��ȥ��
	public ArrayList<StaticWarning> refineWarningWithoutCode ( ArrayList<StaticWarning> warningInfoList ){
		ArrayList<StaticWarning> newWarningInfoList = new ArrayList<StaticWarning> ();
		
		for ( int i =0; i < warningInfoList.size(); i++ ){
			StaticWarning warning = warningInfoList.get(i);
			boolean hasCode = false;
			for ( int j =0; j < warning.getBugLocationList().size(); j++ ){
				ArrayList<String> codeInfoList = warning.getBugLocationList().get(j).getCodeInfoList();
				if ( codeInfoList.size() != 0 && !codeInfoList.get(0).trim().equals( "")){
					hasCode = true;
				}
			}
			if ( hasCode ){
				newWarningInfoList.add( warning );
			}
		}
		return newWarningInfoList;
	}
	
	//��Ҫ�����иó��򣬽����õ�warning Categoryɾ����
	//Ҳ���漰���������͵�refine������ĳ��category�����ĳ��type��
	//�ڵ������о���Ҳ���õ�
	//���cass��Ŀ����UUF_UNUSED_FIELD type��warningȥ����
	public ArrayList<StaticWarning> refineWarningInfoListStyle (  ArrayList<StaticWarning> warningInfoList ){
		//�õ����е�warn����
		ArrayList<StaticWarning> refinedWarningList = new ArrayList<StaticWarning>();
		for ( int i =0; i < warningInfoList.size(); i++ ){
			String type = warningInfoList.get( i ).getBugInfo().getType();
			if ( !type.trim().equals( "UUF_UNUSED_FIELD")){
				refinedWarningList.add( warningInfoList.get( i ));
			}
		}
		
		System.out.println ( "refined warning list size: " + refinedWarningList.size() ); 
		return refinedWarningList;
	}
	
	
	//����һ�εõ�����method�ģ���WarningCharacteristics�е���
	public HashMap<String, Integer> obtainWarningNumberForMethod ( ArrayList<StaticWarning> warningInfoList ){
		HashMap<String, Integer> warningNumberForMethod = new HashMap<String, Integer>();
		for ( int i = 0; i < warningInfoList.size(); i++ ){
			StaticWarning warning = warningInfoList.get( i );
			ArrayList<BugLocation> bugLocationList = warning.getBugLocationList();
			
			//��Ϊ��bugLocationList��������һ��method����Ķദλ�ã�Ӧ������һ��warning��������Ҫ�ҵ���ͬ��method
			Set<String> methodSet = new HashSet<String>();
			for ( int j = 0; j < bugLocationList.size(); j++ ){
				BugLocation bugLoc = bugLocationList.get( j );
				String method = bugLoc.getRelatedMethodName();
				if ( method.equals( ""))
					continue;
				
				String name = bugLoc.getClassName();
				name = name +"-" + method;
				
				methodSet.add( name );
			}
			for ( String methodName: methodSet){
				int num = 1;
				if ( warningNumberForMethod.containsKey( methodName )){
					num += warningNumberForMethod.get( methodName );
				}
				warningNumberForMethod.put( methodName, num );
			}
		}		
		return warningNumberForMethod;
	}
	
	//����һ�εõ�����warningType��
	public HashMap<String, Integer> obtainWarningNumberForWarnType ( ArrayList<StaticWarning> warningInfoList ){
		HashMap<String, Integer> warningNumberForType = new HashMap<String, Integer>();
		for ( int i = 0; i < warningInfoList.size(); i++ ){
			StaticWarning warning = warningInfoList.get( i );
			BugInfo bugInfo = warning.getBugInfo();
			String warnType = bugInfo.getType();
			
			int num = 1;
			if ( warningNumberForType.containsKey( warnType )){
				num += warningNumberForType.get( warnType );
			}
			warningNumberForType.put( warnType, num );
		}
		
		int total = 0;
		for ( String key: warningNumberForType.keySet() ){
			int value = warningNumberForType.get( key );
			total += value;
		}
		
		warningNumberForType.put( "total", total);
		
		return warningNumberForType;
	}	
	
	/*
	 * ����StaticWarningInfo
	 */
	public StaticWarningInfo obtainWarningTypeCategoryInfo ( ArrayList<StaticWarning> warning ){
		StaticWarningInfo warningInfo = new StaticWarningInfo();
		for ( int i = 0; i< warning.size(); i++ ){
			StaticWarning warningDetail = warning.get( i );
			String category = warningDetail.getBugInfo().getCategory();
			String type = warningDetail.getBugInfo().getType();
			
			warningInfo.getCategoryList().add( category );
			if ( !warningInfo.getTypeToCateogoryMap().containsKey( type ) ){
				warningInfo.getTypeToCateogoryMap().put( type, category );
				
				if ( warningInfo.getTypeInCategoryList().containsKey( category ))
					warningInfo.getTypeInCategoryList().get(category).add( type );
				else{
					HashSet<String> typeList = new HashSet<String>();
					typeList.add( type );
					warningInfo.getTypeInCategoryList().put( category, typeList );
				}
			}			
		}
		return warningInfo;
	}
	
	
	public static void main ( String args[] ){
		WarningParser parser = new WarningParser();
		String folderName = "D://java-workstation//lucene2.9.2//src//";
		ArrayList<StaticWarning> warningInfoList  = parser.parseFingbugsWarnings( "data/warning.xml" );
		//parser.obtainWarningNumberForMethod(warningInfoList);
		//parser.obtainWarningNumberForWarnType(warningInfoList);
		//parser.refineWarningInfoList(warningInfoList);
		//parser.obtainCodeInfo( "D://java-workstation//lucene2.9.2//src//");
	}
}
