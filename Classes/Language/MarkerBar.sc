MarkerArea {
	var uView, <dimensions, markerColor, markerSize=5, <coords, currentMarker,
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
		coords = Array.new;
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
					coords.do{ |coord,ind|
						JPen.addArc(coord, markerSize, 0, 2pi);
						JPen.fill;
					};
				};
			});
	}
	moveMarker { |coord,mod|
		var conf, ind;
		conf = this.getConflictPoint(coord);
		conf.isNil.if{ 
			ind = coords.lastIndex;
		}{ 
			ind = conf;
		};
		// probably not so cool to iterate over all points twice here.
		if(this.countConflicts(coord) < 2){ 
			coords.removeAt(ind);
			this.addMarker(coord,mod); 
		};
	}
	addMarker { |coord,mod|
		var add=true;
		add = this.pointIsUnique(coord) && (coords.size < maxSize);
		add.if{ coords = coords.add(coord);	};
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
		rem.notNil.if{ coords.removeAt(rem) };
		uView.refresh;
	}
	getConflictPoint { |coord|
		var hit=nil;
		if(coords.size > 0){
			coords.do{ |obj,ind|
					this.pointCollision(coord,obj).if{ hit = ind;};
			};
		};
		^hit;
	}
	countConflicts { |coord|
		var num=0;
		if(coords.size > 0){
			coords.do{ |obj,ind|
				this.pointCollision(coord,obj).if{ num = num + 1; };
			};
		};
		^num;	
	}
	pointIsUnique { |coord|
		^this.getConflictPoint(coord).isNil;
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
                             
                 