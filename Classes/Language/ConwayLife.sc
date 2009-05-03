ConwayLife {
	var tempField, field, <>fieldSize=64, <>bounds, fieldView;
	*new { |parentArg, boundsArg|
		^super.new.init_conwaylife(parentArg, boundsArg);
	}
	init_conwaylife { |parent, boundsArg|
		bounds = boundsArg ? Rect.new(0, 0, 100, 100);
		tempField = Array.fill(fieldSize, {
			Array.fill(fieldSize, { 0; });
		});
		field = tempField;
		drawFunction = {
			JPen.use{
				field.do{ |rowObj,rowInd|
					rowObj.do{ |colObj,colInd|
						if(colObj == 1){
							var x, y, width, height;
							x = (colInd / fieldSize) * bounds.bounds.width;
							y = (rowInd / fieldSize) * bounds.bounds.height;
							width = fieldSize / bounds.bounds.width;
							height = fieldSize / bounds.bounds.height;
							JPen.addRect(Rect.new(x, y, width, height));
						};
					};
				};
			};
		};
		this.makeGUI(parent, bounds);
	}
/*
	1.	Any live cell with fewer than two live neighbours dies, as if caused by underpopulation.
	2.	Any live cell with more than three live neighbours dies, as if by overcrowding.
	3.	Any live cell with two or three live neighbours lives on to the next generation.
	4.	Any dead cell with exactly three live neighbours becomes a live cell.
*/
	nextGeneration {
		field.do{ |rowObj, rowInd|
			rowObj.do{ |colObj, colInd|
				var count=0;
				if(tempField[(rowInd - 1) % fieldSize][(colInd - 1) % fieldSize] == 1){
					count = count + 1;
				};
				if(tempField[(rowInd - 1) % fieldSize][colInd] == 1){
					count = count + 1;
				};
				if(tempField[(rowInd - 1) % fieldSize][(colInd + 1) % fieldSize] == 1){
					count = count + 1;
				};
				if(tempField[rowInd][(colInd - 1) % fieldSize] == 1){
					count = count + 1;
				};
				if(tempField[rowInd][(colInd + 1) % fieldSize] == 1){
					count = count + 1;
				};
				if(tempField[(rowInd + 1) % fieldSize][(colInd - 1) % fieldSize] == 1){
					count = count + 1;
				};
				if(tempField[(rowInd + 1) % fieldSize][colInd] == 1){
					count = count + 1;
				};
				if(tempField[(rowInd + 1) % fieldSize][(colInd + 1) % fieldSize] == 1){
					count = count + 1;
				};
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
		fieldView = GUI.userView.new(parent, bounds)			.background_(Color.black.alpha_(0.8))
			.relativeOrigin_(false)
			.drawFunc_(drawFunction);
	}
	
}
