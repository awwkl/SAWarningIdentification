package com.comon;

import java.util.ArrayList;
import java.util.HashMap;

/*
 * ����Ŀǰʵ�����������ܣ�һ���ǵõ���ĳ����Ŀ��ص�package��class��Ϣ��
 * ���ǵõ���ÿ��package��class�е�static warning����Ŀ
 * 
 * ��Щʱ�������߲���Ψһ��Ӧ�ģ�һ��file�п��ܺ��ж��class
	/*
	 * <ClassStats class="org.apache.lucene.benchmark.byTask.feeds.DocMaker" sourceFile="DocMaker.java" interface="false" size="188" bugs="1" priority_2="1"/>
      <ClassStats class="org.apache.lucene.benchmark.byTask.feeds.DocMaker$1" sourceFile="DocMaker.java" interface="false" size="4" bugs="0"/>
      <ClassStats class="org.apache.lucene.benchmark.byTask.feeds.DocMaker$DateUtil" sourceFile="DocMaker.java" interface="false" size="11" bugs="0"/>
      <ClassStats class="org.apache.lucene.benchmark.byTask.feeds.DocMaker$DocState" sourceFile="DocMaker.java" interface="false" size="51" bugs="0"/>
      <ClassStats class="org.apache.lucene.benchmark.byTask.feeds.DocMaker$LeftOver" sourceFile="DocMaker.java" interface="false" size="10" bugs="0"/>
      	����������ڲ��࣬
      	
      	$1�� $2���ֱ����������ʽ����
      	protected SpatialStrategy makeSpatialStrategy(final Config config) {
    //A Map view of Config that prefixes keys with "spatial."
    Map<String, String> configMap = new AbstractMap<String, String>() {
      @Override
      public Set<Entry<String, String>> entrySet() {
        throw new UnsupportedOperationException();
      }

      @Override
      public String get(Object key) {
        return config.get("spatial." + key, null);
      }
    };
      	
      	iterfaceҲ����Ϊclass
      	Ҳ����˵���Ҫͳ��һ��file��static warning��Ŀ����Ҫ���кϲ�
	 */

public class ProjectInfo {
	//���ִ洢��ʽ�൱���ظ��洢����Ϣ
	ArrayList<String> packageList;
	HashMap<String, Integer> warningNumForPackage;
	
	HashMap<String, ArrayList<String>> fileListForPackage;
	HashMap<String, Integer> warningNumForFile;
	HashMap<String, Integer> classNumForFile;          //һ���ļ��������Ŀ�������ڲ���
	HashMap<String, String> filePackageNameMap;      //��֪fileName�õ�packageName
	
	Integer totalWarningCount;
	 
	public ProjectInfo ( ){	
		packageList = new ArrayList<String>();
		warningNumForPackage = new HashMap<String, Integer>();
		
		fileListForPackage = new HashMap<String, ArrayList<String>>();
		warningNumForFile = new HashMap<String, Integer>();
		classNumForFile = new HashMap<String, Integer>() ;
		
		filePackageNameMap = new HashMap<String, String>();
		
		totalWarningCount = 0;
	}

	public void printProjectInfo() {
		System.out.println("--- Printing packageList ---");
		for (String s: packageList) 
			System.out.println(s);

		System.out.println("--- Printing warningNumForPackage ---");
		for (String key: warningNumForPackage.keySet()) 
			System.out.println(key + ": " + warningNumForPackage.get(key));

		System.out.println("--- Printing fileListForPackage ---");
		for (String pkg : fileListForPackage.keySet())
			for (String fileName : (fileListForPackage.get(pkg)))
				System.out.println("Package: " + pkg + ", File: " + fileName);
		
		System.out.println("--- Printing warningNumForFile ---");
		for (String key: warningNumForFile.keySet()) 
			System.out.println(key + ": " + warningNumForFile.get(key));

		System.out.println("--- Printing classNumForFile ---");
		for (String key: classNumForFile.keySet()) 
			System.out.println(key + ": " + classNumForFile.get(key));

		System.out.println("--- Printing filePackageNameMap ---");
		for (String key: filePackageNameMap.keySet()) 
		System.out.println(key + ": " + filePackageNameMap.get(key));
		
		System.out.println("--- Printing totalWarningCount ---");
		System.out.println("totalWarningCount: " + totalWarningCount);
	}
	
	public ArrayList<String> getPackageList() {
		return packageList;
	}

	public void setPackageList(ArrayList<String> packageList) {
		this.packageList = packageList;
	}

	public HashMap<String, Integer> getWarningNumForPackage() {
		return warningNumForPackage;
	}

	public void setWarningNumForPackage(HashMap<String, Integer> warningNumForPackage) {
		this.warningNumForPackage = warningNumForPackage;
	}

	public HashMap<String, ArrayList<String>> getFileListForPackage() {
		return fileListForPackage;
	}

	public void setFileListForPackage(HashMap<String, ArrayList<String>> fileListForPackage) {
		this.fileListForPackage = fileListForPackage;
	}

	public HashMap<String, Integer> getWarningNumForFile() {
		return warningNumForFile;
	}

	public void setWarningNumForFile(HashMap<String, Integer> warningNumForFile) {
		this.warningNumForFile = warningNumForFile;
	}

	public HashMap<String, Integer> getClassNumForFile() {
		return classNumForFile;
	}

	public void setClassNumForFile(HashMap<String, Integer> classNumForFile) {
		this.classNumForFile = classNumForFile;
	}


	public HashMap<String, String> getFilePackageNameMap() {
		return filePackageNameMap;
	}


	public void setFilePackageNameMap(HashMap<String, String> filePackageNameMap) {
		this.filePackageNameMap = filePackageNameMap;
	}


	public Integer getTotalWarningCount() {
		return totalWarningCount;
	}


	public void setTotalWarningCount(Integer totalWarningCount) {
		this.totalWarningCount = totalWarningCount;
	}
	
	
}
