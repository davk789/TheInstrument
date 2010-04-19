MarkerBar {
	var <highlightRange, visibleMarkers, markers, <>markerColor, <>hiliteMarkerColor, <>hiliteBackground, values, dimensions, uView, <start, <end, >mouseDownAction, >mouseUpAction, >mouseMoveAction;
	*new { |view, dim|
		^super.new.init_markerbar(view, dim);
	}

	init_markerbar { |view, dim|
		start = 0; // start of visible marker range
		end = 1;   // end of visible marker range
		dimensions = dim;
		markers = Array.new;
		values = Array.new;
		mouseDownAction = { |obj,x,y,mod| };
		mouseUpAction = { |obj,x,y,mod| };
		mouseMoveAction = { |obj,x,y,mod| };
		hiliteBackground = Color.green(0.3);
		markerColor = Color.white;
		hiliteMarkerColor = Color.yellow;
		highlightRange = Dictionary.new;
		uView = GUI.userView.new(view, dimensions)
			.background_(Color.black.alpha_(0.8))
			.relativeOrigin_(true)
			.mouseMoveAction_({ |obj,x,y,mod| 
				mouseMoveAction.value(obj,x,y,mod); 
			})
			.mouseDownAction_({ |obj,x,y,mod| 
				mouseDownAction.value(obj,x,y,mod); 
			})
			.mouseUpAction_({ |obj,x,y,mod|
				mouseUpAction.value(obj,x,y,mod);
				this.markerUpdate(x);
				uView.refresh;
			})
			.drawFunc_({ this.drawView; });
	}
	
	drawView {
		var rangeLo, rangeHi;
		Pen.use{
			Pen.width = 3;
			if((highlightRange['low'].notNil) && (highlightRange['high'].notNil)){
				rangeLo = this.getHighlightCoord(highlightRange['low']);
				rangeHi = this.getHighlightCoord(highlightRange['high']) - rangeLo;
				Pen.color = hiliteBackground;
				Pen.addRect(Rect.new(rangeLo, 0, rangeHi, dimensions.height + 10));
				Pen.fill;
			};
			
			visibleMarkers.do{ |val,ind|
				if(highlightRange['low'].notNil && highlightRange['high'].notNil){
					if((ind >= highlightRange['low']) && (ind <= highlightRange['high'])){
						Pen.color = hiliteMarkerColor;
					}{
						Pen.color = markerColor;
					};
				}{
					Pen.color = markerColor;
				};
				Pen.moveTo(val @ 0);
				Pen.lineTo(val @ (dimensions.height + 10));
				Pen.stroke;
			};
		};
	}
	
	getNearestIndex { |val| // return the last marker index below the location (range == 0..1)
		var ret,ind=0;
		if(val > values.last){
			ret = values.lastIndex;
		}{
			while({ val > values[ind] }, {
				ret = ind;
				ind = ind.increment;
			});
		};
		
		if(ret.isNil){
			ret = -1;
		};
		^ret;
	}
	
	getNearestCoordBelow { |coord|
		^values[this.getNearestIndex(coord)].max(0);
	}
	
	getNearestCoordAbove { |coord|
		var ret;
		ret = values[this.getNearestIndex(coord) + 1].max(0);
		if(ret.notNil){
			^ret;
		}{
			^1;
		};
	}
	
	getHighlightCoord { |ind|
		case{ ind < 0 }{ ^0; }
		{ind > visibleMarkers.lastIndex}{ ^uView.bounds.width; }
		{ ^visibleMarkers[ind] };
	}	
		
	setHighlightRange { |lo,hi| // index not location points
		highlightRange = Dictionary[
			'low'  -> lo,
			'high' -> hi
		];
		uView.refresh;
	}

	getHighlightRange {
		if(values.size > 0){
			^ highlightRange;
		}{
			^Dictionary.new;
		};
	}

	setHighlightCoords { |lo,hi|
		var start, end;

		start = this.getNearestIndex(lo);
		end = this.getNearestIndex(hi) + 1;

		this.setHighlightRange(start, end);
		
	}
		
	getHighlightCoords { // location points not index
		if(values.size > 0){
			^Dictionary[
				'low' -> values[highlightRange['low']],
				'high' -> values[highlightRange['high']]
			
			];
		}{
			^Dictionary.new;
		};
	}
	
	clearHighlightRange {
		highlightRange = Dictionary.new;
		uView.refresh;
	}
	
	zoom_ { |zoomParams|
		this.setZoom(zoomParams[0], zoomParams[1]);
	}
	
	zoom {
		^[start, end];
	}
	
	setZoom { |x, y|
		start = min(x, y);
		end = max(x, y);
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
			markers = markers.add(scaledX).sort;
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
	
	clear {
		markers = visibleMarkers = values = Array.new;
		highlightRange = Dictionary.new;
		uView.refresh;
	}

	background_ { |color|
		uView.background = color;
	}
	
	background {
		^uView.background;
	}
	

	
}


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
				Pen.use{
					Pen.color = markerColor;
					prCoords.do{ |coord,ind|
						if(ind == prCurrentIndex){ Pen.color_(selectionColor) };
						Pen.addArc(coord, markerSize, 0, 2pi);
						Pen.fill;
						if(ind == prCurrentIndex){ Pen.color_(markerColor) };
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
		rem = this.getConflictPoint(coord);
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

