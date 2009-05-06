ConwayLife {
	var <tempField, <field, <>fieldSize=8, <>bounds, <fieldView, drawAction, drawFunction, rate=10, <>waitTime=0.1;
	*new { |parentArg, boundsArg|
		^super.new.init_conwaylife(parentArg, boundsArg);
	}
	*test {
		var testWin;
		testWin = GUI.window.new("test life", Rect.new(500.rand, 500.rand, 400, 400)).front;
		^super.new.init_conwaylife(testWin, Rect.new(0, 0, 400, 400));
	}
	init_conwaylife { |parent, boundsArg|
		bounds = boundsArg ? Rect.new(0, 0, 100, 100);
		tempField = Array.fill(fieldSize, {
			Array.fill(fieldSize, { 0; });
		});
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
		this.makeGUI(parent, bounds);
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
	nextGeneration {
		field.do{ |rowObj, rowInd|
			rowObj.do{ |colObj, colInd|
				var count=0;
				count = tempField[(rowInd - 1) % fieldSize][(colInd - 1) % fieldSize] + 
					tempField[(rowInd - 1) % fieldSize][colInd] + 
					tempField[(rowInd - 1) % fieldSize][(colInd + 1) + 
					tempField[rowInd][(colInd - 1) % fieldSize] + 
					tempField[rowInd][(colInd + 1) % fieldSize] + 
					tempField[(rowInd + 1) % fieldSize][(colInd - 1) % fieldSize] + 
					tempField[(rowInd + 1) % fieldSize][colInd] + 
					tempField[(rowInd + 1) % fieldSize][(colInd + 1) % fieldSize];
				case{count < 2}{
					tempField[rowInd][colInd] = 0;
				}
				{ count > 3 }{
					tempField[rowInd][colInd] = 0;
				}
				{ (count == 3) && (colObj == 0)}{
					tempField[rowInd][colInd] = 1;
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
				drawAction.(x,y)
			})
		    .mouseUpAction_({ |obj,x,y,mod| drawAction.(x,y) })
		    .mouseMoveAction_({ |obj,x,y,mod| drawAction.(x,y)});
	}
	
}
