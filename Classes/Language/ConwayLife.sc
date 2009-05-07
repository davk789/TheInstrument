ConwayLife {
	var <tempField, <field, <>fieldSize, <>bounds, <fieldView, drawAction, drawFunction, rate=10, <>waitTime=0.1, <>prRule, <ruleSets;
	*new { |fieldSizeArg, parent, boundsArg|
		^super.new.init_conwaylife(fieldSizeArg, parent, boundsArg);
	}
	*test { |fieldSizeArg|
		var testWin;
		testWin = GUI.window.new("test life", Rect.new(500.rand, 500.rand, 400, 400)).front;
		^super.new.init_conwaylife(fieldSizeArg, testWin, Rect.new(0, 0, 400, 400));
	}
	init_conwaylife { |fieldSizeArg, parent, boundsArg|
		bounds = boundsArg ? Rect.new(0, 0, 100, 100);
		fieldSize = fieldSizeArg ? 16;
		tempField = Array.fill(fieldSize, {
			Array.fill(fieldSize, { 0; });
		});
		prRule = [[2,3],[3]];
		/* 
			taken from http://en.wikipedia.org/wiki/Life-like_cellular_automaton
		*/
		ruleSets = Dictionary[ 
			'Seeds' -> [[],[2]],
			'Serviettes' -> [[],[2,3,4]],
			'Wolfram9a' -> [[0,1,2,3,4,5,6,7,8],[3,7,8]],
			'Wolfram7d' -> [[0,1,3,5,6],[1,3,4,5,6]],
			'Wolfram13c2' -> [[0,1,8],[0,1,8]],
			'Wolfram13f3' -> [[0,2,3,8],[1,2,3,5,6,7]],
			'Wolfram7g' -> [[0,3,4,5,6],[3,4]],
			'Wolfram7i' -> [[0,4,5],[0,5,7,8]],
			'Wolfram7a' -> [[0,4,6,8],[2,3,6]],
			'Gnarl' -> [[1],[1]],
			'Maze' -> [[1,2,3,4,5],[3]],
			'Wolfram7h' -> [[1,2,4,5,6],[0,5,7,8]],
			'2x2' -> [[1,2,5],[3,6]],
			'Wolfram13h' -> [[1,3,5],[1,3,5]], 
			'Replicator' -> [[1,3,5,7],[1,3,5,7]], 
			'Amoeba' -> [[1,3,5,8],[3,5,7]], 
			'Life' -> [[2,3],[3]], 
			'HighLife' -> [[2,3],[3,6]], 
			'Wolfram9c' -> [[2,3,4,6],[3,6,7]], 
			'Stains' -> [[2,3,5,6,7,8],[3,6,7,8]], 
			'Coagulations' -> [[2,3,5,6,7,8],[3,7,8]], 
			'PseudoLife' -> [[2,3,8],[3,5,7]], 
			'Move' -> [[2,4,5],[3,6,8]], 
			'Wolfram7b' -> [[2,7],[2,5,7]], 
			'34 Life' -> [[3,4],[3,4]], 
			'DayAndNight' -> [[3,4,6,7,8],[3,6,7,8]], 
			'Assimilation' -> [[4,5,6,7],[3,4,5]], 
			'Wolfram7f' -> [[4,5,6,7,8],[1,3,7]], 
			'Coral' -> [[4,5,6,7,8],[3]], 
			'LongLife' -> [[5],[3,4,5]], 
			'Diamoeba' -> [[5,6,7,8],[3,5,6,7,8]]
		];
		field = tempField;
		drawAction = { |x,y|
			[x,y].postln;
		};
		drawFunction = {
			Pen.use{
				field.do{ |rowObj,rowInd|
					rowObj.do{ |colObj,colInd|
						var x, y, width=10, height=10;
						if(colObj == 1){
							Pen.color = Color.black;
							Pen.fill;
							x = (colInd / fieldSize) * bounds.bounds.width;
							y = (rowInd / fieldSize) * bounds.bounds.height;
							width = bounds.bounds.width / fieldSize;
							height = bounds.bounds.height / fieldSize;
							Pen.addRect(Rect.new(x, y, width, height));
						};
					};
				};
			};
		};
		if(parent.notNil){
			this.makeGUI(parent, bounds);	
		};
	}
	getIndexFromCoords { |rowArg,colArg|
		var ret;
		ret = Array.new;
		ret = ret.add(((rowArg / bounds.bounds.height) * fieldSize).asInt);
		ret = ret.add(((colArg / bounds.bounds.width) * fieldSize).asInt);
		^ret;
	}
	setDrawAction { |col,row|
		var fieldIndex;
		fieldIndex = this.getIndexFromCoords(row,col);
		fieldIndex.postln;
		if(field[fieldIndex[0]][fieldIndex[1]] == 1){
			drawAction = { |posX,posY|
				var fIndex;
				fIndex = this.getIndexFromCoords(posY,posX);
				tempField[fIndex[0]][fIndex[1]] = 0;
				field = tempField;
				fieldView.refresh;
			};
		}{
			drawAction = { |posX,posY|
				var fIndex;
				fIndex = this.getIndexFromCoords(posY,posX);
				tempField[fIndex[0]][fIndex[1]] = 1;
				field = tempField;
				fieldView.refresh;
			};
		};
	}
	refreshRate_ { |val|
		rate = val;
		waitTime = rate.reciprocal;
	}
	refreshRate {
		^rate;
	}
	rule_ { |val|
		if(val.isArray){
			prRule = val;
		}{
			if(ruleSets[val].notNil){
				prRule = ruleSets[val];
			}{
				"no rule, doing nothing".postln;
			};
		};
	}
	start {
		waitTime = rate.reciprocal;
		AppClock.sched(0, {
			this.nextGeneration;
			fieldView.refresh;
			waitTime;
		});
	}
	stop {
		waitTime = nil;
	}
	getNextGeneration {
		this.nextGeneration;
		if(fieldView.notNil){
			fieldView.refresh;
		};
		^field;
	}
	nextGeneration {
		field.do{ |rowObj, rowInd|
			rowObj.do{ |colObj, colInd|
				var count=0;
				count = field[(rowInd - 1) % fieldSize][(colInd - 1) % fieldSize] + 
					field[(rowInd - 1) % fieldSize][colInd] + 
					field[(rowInd - 1) % fieldSize][(colInd + 1) % fieldSize] + 
					field[rowInd][(colInd - 1) % fieldSize] + 
					field[rowInd][(colInd + 1) % fieldSize] + 
					field[(rowInd + 1) % fieldSize][(colInd - 1) % fieldSize] + 
					field[(rowInd + 1) % fieldSize][colInd] + 
					field[(rowInd + 1) % fieldSize][(colInd + 1) % fieldSize];
				if(colObj == 0){
					if(prRule[1].includes(count)){
						tempField[rowInd][colInd] = 1;
					};
					if(prRule[1].includes(0)){
						tempField[rowInd][colInd] = 1;
					};
				}{
					if(prRule[0].includes(count).not){
						tempField[rowInd][colInd] = 0;
					};
					if(prRule[0].includes(0)){
						tempField[rowInd][colInd] = 0;
					};

				};
			};
		};
		field = tempField;
	}
	makeGUI { |parent, bounds|
		fieldView = GUI.userView.new(parent, bounds)
            .background_(Color.white)
			.relativeOrigin_(false)
			.drawFunc_(drawFunction)
		    .mouseDownAction_({ |obj,x,y,mod|
				this.setDrawAction(x,y);
				drawAction.(x,y);
				if(mod == 131330){
					this.stop;
				};
				
			})
		    .mouseUpAction_({ |obj,x,y,mod| 
		    	drawAction.(x,y);
		    	if(mod == 131330){
		    		this.start;
		    	};
		    })
		    .mouseMoveAction_({ |obj,x,y,mod| drawAction.(x,y)});
	}
	
}
