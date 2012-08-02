package org.cheminfo.scripting.image.tests;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import junit.framework.Assert;

import org.cheminfo.function.scripting.ScriptingInstance;
import org.cheminfo.scripting.image.EIJ;
import org.cheminfo.scripting.image.IJ;
import org.json.JSONObject;
import org.junit.Test;

public class TestScripting {

	@Test
	public void test() {
		// You can run this method for testing the behavior of you functions
		// inside javascript.
		// Before using this function, build the .jar by running build.xml. Only
		// so you will see the changes made in Image.java
		ScriptingInstance interpreter = new ScriptingInstance(
				"./workspace/imagePlugin/jars/");

		JSONObject result = interpreter
				.runScript("var result=Image.helloWorld('Castillo'); jexport('theNametoShow',result)");

		Assert.assertEquals(
				"{\"result\":{\"theNametoShow\":\"Castillo, Hello World!\"}}",
				result.toString());

		// It has to print "Castillo, Hello World!"
		System.out.println(((JSONObject) result.get("result"))
				.get("theNametoShow"));
	}

	/**
	 * Run this Jorge!!!!!
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		ScriptingInstance interpreter = new ScriptingInstance("jars/");
		interpreter.setSafePath("./");
		String script = getContents(new File("XTC/imagetestsplit.js"));
		JSONObject result = interpreter.runScript(script);
		System.out.println(result);

	}

	static public String getContents(File aFile) {
		try {
			System.out.println(aFile.getCanonicalPath());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// ...checks on aFile are elided
		StringBuilder contents = new StringBuilder();

		try {
			// use buffering, reading one line at a time
			// FileReader always assumes default encoding is OK!
			BufferedReader input = new BufferedReader(new FileReader(aFile));
			try {
				String line = null; // not declared within while loop
				/*
				 * readLine is a bit quirky : it returns the content of a line
				 * MINUS the newline. it returns null only for the END of the
				 * stream. it returns an empty String if two newlines appear in
				 * a row.
				 */
				while ((line = input.readLine()) != null) {
					contents.append(line);
					contents.append(System.getProperty("line.separator"));
				}
			} finally {
				input.close();
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		return contents.toString();
	}

}
