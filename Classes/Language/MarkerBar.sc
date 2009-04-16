MarkerArea {
	var uView, prCoords, prCurrentIndex, updateCurrentIndex=true,
		<dimensions, markerColor, selectionColor, markerSize=5, currentMarker,
		<>mouseDownAction, <>mouseUpAction, <>mouseMoveAction, <>maxSize=8;
	*new { |view, dim|
		^super.new.init_markerarea(view, dim);
	}
	*test {
		var win;
		win = GUI.window.new("asd", Rect.new(200.rand, 200.rand, 150, 150)).front;
		^super.new.init_markerarea(win, Rect.new(0, 0, 145, 145));
	}
	init_markerarea { |view, dim|
		dimensions = dim;
		markerColor = Color.yellow;
		selectionColor = Color.green;
		prCoords = Array.new;
		mouseDownAction = { |obj,x,y,mod| };
		mouseUpAction = { |obj,x,y,mod| };
		mouseMoveAction = { |obj,x,y,mod| };
		uView = GUI.userView.new(view, dimensions)
			.background_(Color.black.alpha_(0.8))
			.relativeOrigin_(false)
			.mouseDownAction_({ |obj,x,y,mod| 
				this.handleAddEvent(x @ y, mod);
				mouseDownAction.(obj,x,y,mod);
			})
			.mouseMoveAction_({ |obj,x,y,mod| 
				this.moveMarker(x @ y, mod); 
				mouseMoveAction.(obj,x,y,mod);
			})
			.mouseUpAction_({ |obj,x,y,mod| 
				mouseUpAction.(obj,x,y,mod);
			})
			.drawFunc_({
				JPen.use{
					JPen.color = markerColor;
					prCoords.do{ |coord,ind|
						if(ind == prCurrentIndex){ JPen.color_(selectionColor) };
						JPen.addArc(coord, markerSize, 0, 2pi);
						JPen.fill;
						if(ind == prCurrentIndex){ JPen.color_(markerColor) };
					};
				};
			});
	}
 	moveMarker { |coord,mod|
		var conf, ind;
		conf = this.getConflictPoint(coord);
		conf.isNil.if{ 
			ind = prCoords.lastIndex;
		}{ 
			ind = conf;
		};
		prCurrentIndex = ind;
		postln("prCurrentIndex = " ++ prCurrentIndex);
		// probably not so cool to iterate over all points twice here.
		if(this.countConflicts(coord) < 2){ 
			prCoords.removeAt(ind);
			this.addMarker(coord,mod); 
		};
	}
	addMarker { |coord,mod|
		if(this.getConflictPoint(coord).isNil && (prCoords.size < maxSize)){
			prCoords = prCoords.add(coord);
		};
		uView.refresh;
	}
	handleAddEvent { |coord,mod|
		if(mod == 131072){ // shift key
			this.removeMarker(coord);
		}{
			this.addMarker(coord);
		};
	}
	removeMarker { |coord|
		var rem;
		rem = this.getConflictPoint(coord)l
		if(rem.notNil){ 
			prCoords.removeAt(rem) 
		};
		uView.refresh;
	}
	getConflictPoint { |coord|
		var hit=nil;
		if(prCoords.size > 0){
			prCoords.do{ |obj,ind|
					this.pointCollision(coord,obj).if{ hit = ind;};
			};
		};
		^hit;
	}
	countConflicts { |coord|
		var num=0;
		if(prCoords.size > 0){
			prCoords.do{ |obj,ind|
				this.pointCollision(coord,obj).if{ num = num + 1; };
			};
		};
		^num;	
	}
	pointCollision { |currentCoord,prevCoord|
		^(
			(
				(currentCoord.x <= (prevCoord.x + markerSize)) 
					&& 
				(currentCoord.y <= (prevCoord.y + markerSize))
			) 
			&& 
			(
				(currentCoord.x > (prevCoord.x - markerSize)) 
					&& 
				(currentCoord.y > (prevCoord.y - markerSize))
			)
		);
	}
	// getter/setter methods
	bounds_ { |val|
		uView.bounds = val;
	}
	bounds {
		^uView.bounds;
	}
	coords_ { |arr|
		prCoords = arr;
		uView.refresh;
	}
	coords {
		^prCoords;
	}
	currentIndex_ { |ind|
		prCurrentIndex = ind;
		uView.refresh;
	}
	currentIndex {
		^prCurrentIndex;
	}

}

MarkerBar {
	var visibleMarkers, markers, <>markerColor, values, dimensions, uView, <start=0, <end=1;
	*new { |view, dim|
		^super.new.init_markerbar(view, dim);
	}
	init_markerbar { |view, dim|
		dimensions = dim;
		markers = Array.new;
		values = Array.new;
		markerColor = Color.black.alpha_(0.8);
		uView = GUI.userView.new(view, dimensions)
			.background_(Color.black.alpha_(0.8))
			.relativeOrigin_(false)
			.mouseUpAction_({ |obj,x,y,mod|
				this.markerUpdate(x);
			})
			.drawFunc_({
				JPen.use{
					JPen.width = 3;
					JPen.color = markerColor;
					visibleMarkers.do{ |val,ind|
						JPen.moveTo(val @ 0);
						JPen.lineTo(val @ (dimensions.height + 10));
						JPen.stroke;
					};
				};
			});
	}
	zoom { |startIn, endIn|
		start = startIn;
		end = endIn;
		this.updateVisibleMarkers;
		uView.refresh;
	}
	updateVisibleMarkers {
		var range, startPoint;
		range = (end - start).reciprocal;
		startPoint = start * dimensions.width;
		visibleMarkers = (markers - startPoint) * range;
	}
	markerUpdate { |x|
		var scaledX;
		scaledX = ((x * (end - start)) + (start * dimensions.width)).round;
		if(markers.indexOf(scaledX).notNil){
			markers.removeAt(markers.indexOf(scaledX));
		}{
			markers = markers.add(scaledX);	
		};
		values = markers * (1 / dimensions.width);
		this.updateVisibleMarkers;
		uView.refresh;
	}
	value {
		^values;
	}
	value_ { |val|
		values = val;
		markers = val * dimensions.width;
		uView.refresh;
	}
	background_ { |color|
		uView.background = color;
	}
}
                             
                 
