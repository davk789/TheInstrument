/*MarkerArea {
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
						JPen.addArc();
						JPen.stroke;
					};
				};
			});
	}
}*/

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
		scaledX.postln;
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
