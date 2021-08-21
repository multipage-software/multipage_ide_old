/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package org.maclan.server;

/**
 * @author
 *
 */
public class Analysis {

	/**
	 * Call counters.
	 */
	protected int process_area_calls = 0;
	protected int list_calls = 0;
	protected int loop_calls = 0;
	protected int procedure_calls = 0;
	protected int call_calls = 0;
	protected int languages_call = 0;
	protected int if_calls = 0;
	protected int superareas_call = 0;
	protected int subareas_calls = 0;
	protected int block_calls = 0;
	protected int pack_calls = 0;
	protected int rem_calls = 0;
	protected int anchor_calls = 0;
	protected int get_calls = 0;
	protected int set_calls = 0;
	protected int var_calls = 0;
	protected int image_calls = 0;
	protected int tag_area_calls = 0;
	protected int tag_calls = 0;
	protected int find_tag_count = 0;
	
	/**
	 * Dump call counts.
	 */
	public void dumpAnalysis() {
		
		System.out.println("#Process analysis:");
		System.out.println("list_calls = " + list_calls);
		System.out.println("loop_calls = " + loop_calls);
		System.out.println("procedure_calls = " + procedure_calls);
		System.out.println("call_calls = " + call_calls);
		System.out.println("languages_call = " + languages_call);
		System.out.println("if_calls = " + if_calls);
		System.out.println("superareas_call = " + superareas_call);
		System.out.println("subareas_calls = " + subareas_calls);
		System.out.println("block_calls = " + block_calls);
		System.out.println("pack_calls = " + pack_calls);
		System.out.println("rem_calls = " + rem_calls);
		System.out.println("anchor_calls = " + anchor_calls);
		System.out.println("get_calls = " + get_calls);
		System.out.println("set_calls = " + set_calls);
		System.out.println("var_calls = " + var_calls);
		System.out.println("image_calls = " + image_calls);
		System.out.println("tag_area_calls = " + tag_area_calls);
		System.out.println("tag_calls = " + tag_calls);
		System.out.println("process_area_calls = " + process_area_calls);
		System.out.println("find_tag_count = " + find_tag_count);
	}

}
