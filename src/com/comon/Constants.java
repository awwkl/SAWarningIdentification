package com.comon;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ca.uwaterloo.ece.qhanam.slicer.Slicer;

public class Constants {
	
	// Set FOLDER_NAME to the root directory where you cloned the target project
	public final static String CURRENT_TIME = "2021-05-21 14:47:25.000000";
	public final static String PROJECT_NAME = "jadx";
	public final static String FOLDER_NAME = "C:\\Users\\65983\\Desktop\\soar_lab\\projects\\jadx\\";

	// Before checking out a specific revision, obtain these values
	public final static String GIVEN_EARLIEST_TIME = "2013-03-18 17:04:23";
	public final static Integer GIVEN_EARLIEST_REVISION_NUMBER = 1402;
	public final static Integer MAX_REVISION_NUMBER = 1402;
	
	// After deciding on the CURRENT_COMMIT_TIME, obtain the REMAINING_REVISIONS
	public final static String CURRENT_COMMIT_TIME 	= "2021-04-01 00:00:00"; 
	public final static int REMAINING_REVISIONS = 1386;
	public final static int CURRENT_REVISION_NUMBER = MAX_REVISION_NUMBER - REMAINING_REVISIONS + 1;
	
	
	/* The variables below are for the input & output data of the tool. They may not need to be changed. */

	// SQL Database table names
	public final static String COMMIT_INFO_TABLE = "SAWI_commit_info";
	public final static String COMMIT_CONTENT_TABLE = "SAWI_commit_content";
	public final static String ISSUE_TABLE = "SAWI_issue_info";

	// Input files
	public final static String WARNING_FILE_NAME = "working/spotbugs.xml";
	public final static String LOG_FILE_IN = "working/log.txt";
	public final static String LOG_CODE_FILE_IN = "working/logCode.txt";

	// Output files
	public final static String LOG_CODE_FOLDER_OUT  = "working/logcode-files/";
	public final static String FEATURE_VALUE_OUTPUT_FOLDER = "working/feature/";

	// Unused files
	public final static String WARNING_FILE_OUT = "working/warning-result.txt";
	public final static String GROUND_TRUTH_FOLDER = "working/groundtruth/";
	//public final static String WARNING_FILE_IN = "working/warning.csv";

	
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
