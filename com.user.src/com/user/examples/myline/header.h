/*******************************************************************************
 *  Copyright (c) 2015 IBM Corporation.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
/*
 * This header file is for demonstration purposes only. The structures below are the
 * C equivalent of the layouts descriptions in the LD file.
 *
 * Ideally, a groveller would parse this file and generate the LD file with layout descriptions
 */
#include "stdint.h"

struct Point {
	uint32_t x;
	uint32_t y;
};

struct Line {
	struct Point start;
	struct Point end;
};
