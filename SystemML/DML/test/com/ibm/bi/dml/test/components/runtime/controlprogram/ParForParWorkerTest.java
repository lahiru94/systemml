package com.ibm.bi.dml.test.components.runtime.controlprogram;


import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;

import junit.framework.Assert;

import org.junit.Test;

import com.ibm.bi.dml.runtime.controlprogram.LocalVariableMap;
import com.ibm.bi.dml.runtime.controlprogram.Program;
import com.ibm.bi.dml.runtime.controlprogram.ProgramBlock;
import com.ibm.bi.dml.runtime.controlprogram.parfor.ParForBody;
import com.ibm.bi.dml.runtime.controlprogram.parfor.ParWorker;
import com.ibm.bi.dml.runtime.controlprogram.parfor.Task;
import com.ibm.bi.dml.runtime.controlprogram.parfor.Task.TaskType;
import com.ibm.bi.dml.runtime.instructions.CPInstructions.IntObject;
import com.ibm.bi.dml.sql.sqlcontrolprogram.ExecutionContext;
import com.ibm.bi.dml.utils.DMLRuntimeException;
import com.ibm.bi.dml.utils.DMLUnsupportedOperationException;

/**
 * TestCases for local task queue.
 * 
 * @author mboehm
 */
public class ParForParWorkerTest extends ParWorker
{
	private static final int _N = 101;
	
	@Test
	public void testExecutionSet() 
		throws InterruptedException, DMLRuntimeException, DMLUnsupportedOperationException 
	{ 
		Collection<Task> tasks = createTasks( TaskType.ITERATION_SET );
		setParWorkerAttributes();
		
		for( Task ltask : tasks )
			executeTask(ltask);
		
		if( _numIters!=_N || _numTasks!=_N )
			Assert.fail("Wrong number of executed tasks or iterations.");
	}
	
	@Test
	public void testExecutionRange() 
		throws InterruptedException, DMLRuntimeException, DMLUnsupportedOperationException 
	{ 
		Collection<Task> tasks = createTasks( TaskType.ITERATION_RANGE );
		setParWorkerAttributes();
		
		for( Task ltask : tasks )
			executeTask(ltask);
		
		if( _numIters!=_N || _numTasks!=1 )
			Assert.fail("Wrong number of executed tasks or iterations.");
	}
	
	
	private Collection<Task> createTasks(TaskType type) 
		throws DMLRuntimeException
	{
		//create N tasks
		Collection<Task> tasks = new LinkedList<Task>();
		
		if( type == TaskType.ITERATION_SET )
		{
			for( int i=1; i<=_N; i++ )
			{
				Task ltask = new Task(TaskType.ITERATION_SET);
				ltask.addIteration(new IntObject("i",i));
				tasks.add(ltask);
	 		}
		}
		else if(type == TaskType.ITERATION_RANGE)
		{
			Task ltask = new Task(TaskType.ITERATION_RANGE);
			ltask.addIteration(new IntObject("i",1));
			ltask.addIteration(new IntObject("i",_N));
			ltask.addIteration(new IntObject("i",1));
			tasks.add(ltask);
		}
		else
			throw new DMLRuntimeException("Undefined task type.");
		
		return tasks;
	}
	
	
	private void setParWorkerAttributes() throws DMLRuntimeException
	{
		Program prog = new Program();
		ArrayList<ProgramBlock> pbs = new ArrayList<ProgramBlock>();
		pbs.add(new ProgramBlock(prog));
		ParForBody body = new ParForBody(pbs,new LocalVariableMap (),new ArrayList<String>(),(ExecutionContext)null);
		
		_workerID = -1;
		_numTasks = 0;
		_numIters = 0;
		_variables = body.getVariables();
		_childBlocks = body.getChildBlocks();
		_ec = body.getEc();
	}
}
