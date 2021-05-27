package com.comon;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.database.DBOperation;
import java.sql.*;
import java.time.*;
import java.util.*;
import java.io.*;
import org.yaml.snakeyaml.*;

import ca.uwaterloo.ece.qhanam.slicer.Slicer;

public class Constants {
	public static final DBOperation dbOperation = new DBOperation();
	
	public static String 	PROJECT_NAME;
	public static String 	FOLDER_NAME;
	public static String 	CURRENT_TIME;

	public static String 	GIVEN_EARLIEST_TIME;
	public static Integer 	GIVEN_EARLIEST_REVISION_NUMBER;
	public static Integer 	MAX_REVISION_NUMBER;

	public static String 	CURRENT_COMMIT_TIME;
	public static int 		REMAINING_REVISIONS;
	public static int 		CURRENT_REVISION_NUMBER;
	
	static {
		try {
			InputStream inputStream = new FileInputStream(new File("data/config.yml"));
			Yaml yaml = new Yaml();
			Map<String, Object> yaml_map = yaml.load(inputStream);
			System.out.println(yaml_map);
			
			PROJECT_NAME 			= (String) yaml_map.get("PROJECT_NAME");
			FOLDER_NAME 			= (String) yaml_map.get("FOLDER_NAME");
			CURRENT_TIME 			= (String) yaml_map.get("CURRENT_TIME");
			
			try {
				String sql = "select min(commitTime) as minTime from " + Constants.COMMIT_INFO_TABLE;
				ResultSet rs = dbOperation.DBSelect(sql);
				while ( rs.next() ) 
					GIVEN_EARLIEST_TIME = rs.getString("minTime");

				sql = "select count(*) as numCommits from " + Constants.COMMIT_INFO_TABLE;
				rs = dbOperation.DBSelect(sql);
				while ( rs.next() ) 
					GIVEN_EARLIEST_REVISION_NUMBER = Integer.parseInt(rs.getString("numCommits"));

				MAX_REVISION_NUMBER = GIVEN_EARLIEST_REVISION_NUMBER;
			} catch (SQLException e) {
				System.out.println("Error with SQL while setting YAML config.");
				e.printStackTrace();
			}

			CURRENT_COMMIT_TIME 	= (String) yaml_map.get("CURRENT_COMMIT_TIME");
			REMAINING_REVISIONS 	= (Integer) yaml_map.get("REMAINING_REVISIONS");
			CURRENT_REVISION_NUMBER = MAX_REVISION_NUMBER - REMAINING_REVISIONS + 1;
			
			System.out.printf("%s, %s, %s \n", CURRENT_TIME, PROJECT_NAME, FOLDER_NAME);
			System.out.printf("%s, %d, %d \n", GIVEN_EARLIEST_TIME, GIVEN_EARLIEST_REVISION_NUMBER, MAX_REVISION_NUMBER);
			System.out.printf("%s, %d, %d \n", CURRENT_COMMIT_TIME, REMAINING_REVISIONS, CURRENT_REVISION_NUMBER);
		} catch (Exception e) {
			System.out.println("Error reading YAML config files.");
			e.printStackTrace();
		}
	}
	
	/* The variables below are for the input & output data of the tool. They may not need to be changed. */
	
	// Directory for input & output data of tool
	public static String WORKING_DIR = "data/";
	
	// Input files
	public static String LOG_FILE_IN = 					WORKING_DIR + "log.txt";
	public static String LOG_CODE_FILE_IN = 			WORKING_DIR + "logCode.txt";
	public static String WARNING_FILE_NAME = 			WORKING_DIR + "spotbugs.xml";

	// Output files
	public static String LOG_CODE_FOLDER_OUT  = 		WORKING_DIR + "logcode-files/";
	public static String FEATURE_VALUE_OUTPUT_FOLDER = 	WORKING_DIR + "feature/";
	public static String GROUND_TRUTH_FOLDER = 			WORKING_DIR + "groundtruth/";

	// Unused files
	public static String WARNING_FILE_OUT = 			WORKING_DIR + "warning-result.txt";
	// public static String WARNING_FILE_IN = 				WORKING_DIR + "warning.csv";

	// SQL Database table names
	public static final String COMMIT_INFO_TABLE = 		"SAWI_commit_info";
	public static final String COMMIT_CONTENT_TABLE = 	"SAWI_commit_content";
	public static final String ISSUE_TABLE = 			"SAWI_issue_info";

	/* The variables below do not need to be changed. */
	
	public static enum BUG_LOCATION_REGION_TYPE{
		CLASS, FIELD, METHOD, TYPE, DEFAULT;
	}
	public final static int FEATURE_SPLIT_SIZE = 20;

	//public final static String FORMER_REVISION_TIME_UNDER_INVEST = "2013-07-01 00:00:01";
	//public final static String LATTER_REVISION_TIME_UNDER_INVEST = "2014-07-01 00:00:01";
	
	public final static String[] WARN_CATEGORY_UNDER_INVESTIGATION = {"MALICIOUS_CODE", "CORRECTNESS", "PERFORMANCE", "SECURITY", 
			"MT_CORRECTNESS", "BAD_PRACTICE", "EXPERIMENTAL", "STYLE", "I18N"};
	//"BAD_PRACTICE", "EXPERIMENTAL",	"STYLE", "I18N" 
	//dodgy codeû�У�����style��Ӧ����������һ����
	
	// correctness, vulnerability, malicious code, security, multithreaded performance, bad practice
	
	public final static Slicer.Direction SLICER_DIRECTION = Slicer.Direction.BACKWARDS;
	public final static Slicer.Type SLICER_TYPE = Slicer.Type.DATA;
	public final static Slicer.Options[] SLICER_OPTIONS = new Slicer.Options[]{Slicer.Options.CONTROL_EXPRESSIONS_ONLY};
	public final static Integer MAX_SLICES = 5;
	
	public final static String JRE_LOCATION  = "C:\\Program Files\\Java\\jre1.8.0_91\\lib\\\rt.jar";
	
	//source code slicer/parser ��ص�?
	public final static String[] BINARY_OPERATION = { "*", "/", "%", "+", "-", "&", "^", "|", "+=", "-=", "*=", "/=", "&=", "^=", "|=", "++", "--"};
	public final static String[] METHOD_FIELD_VISIBILITY = { "public", "private", "protected"};
	public final static String[] METHOD_FIELD_TYPE = { "static", "final", "abstract" };
	public final static String[] CLASS_TYPE = { "abstract", "interface", "array" };
	
	public final static Integer SPLIT_NUM = 5;
	
	public final static String[] CATEGORIAL_FEATURES = { "F26", "F53", "F54", "F55",
			"F20", "F21", "F22", 
			"F1", "F2", "F3", "F4", "F5", "F6", "F7", "F8", "F9", "F10", "F11", "F12", "F13", "F14", "F15", "F16", "F17", "F18", "F19",
			 "F71" };
	public final static List CATEGORIAL_FEATURES_LIST = Arrays.asList( com.comon.Constants.CATEGORIAL_FEATURES);
	
	public final static String[] NUMERICAL_FEATURES = { "F25",  "F72",
			"F23", "F61", "F94", "F95",
			"F34",  "F62", "F64", "F65", "F66", "F67", "F68", "F69", "F101", "F102", "F103", "F104", "F105", "F106", "F107", "F108",
			"F35", "F36", "F37", "F38", "F39", "F40", "F41", "F42", "F43", "F44", "F45", "F46", "F47", "F48", "F49", "F50", "F51", "F52",
			"F126", "F127", "F128", "F129", "F130", "F131", "F132", "F133", "F134", "F135", "F136", "F137", "F138", "F139", "F140", "F141", "F142", "F143",
			"F70", "F73", "F74", "F146", "F147", "F83", "F84", "F85",
			"F77", "F88", "F109", "F110", "F111", "F112", "F113", "F114", "F115", "F116", "F117", "F118", "F119", "F120", "F121", "F122", "F123"};
	public final static List NUMERICAL_FEATURES_LIST = Arrays.asList( com.comon.Constants.NUMERICAL_FEATURES);
	
	public final static String[] UNUSED_FEATURES = { "F47", "F48", "F49", "F50", "F51", "F52", "F138", "F139", "F140", "F141", "F142", "F143",
			 "F85", "F147", "F53", };
	public final static List UNUSED_FEATURES_LIST = Arrays.asList( com.comon.Constants.UNUSED_FEATURES);
}
