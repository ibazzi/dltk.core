/*******************************************************************************
 * Copyright (c) 2005, 2016 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
package org.eclipse.dltk.ti.goals;

import org.eclipse.dltk.ti.GoalState;

public class NullGoalEvaluator extends GoalEvaluator {

	public NullGoalEvaluator(IGoal goal) {
		super(goal);
	}

	@Override
	public IGoal[] init() {
		return IGoal.NO_GOALS;
	}

	@Override
	public Object produceResult() {
		return null;
	}

	@Override
	public IGoal[] subGoalDone(IGoal subgoal, Object result, GoalState state) {
		return IGoal.NO_GOALS;
	}

}
