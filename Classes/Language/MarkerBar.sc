MarkerBar {
	var markers, <>markerColor, values, dimensions, uView;
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
					markers.do{ |val,ind|
						JPen.moveTo(val @ 0);
						JPen.lineTo(val @ (dimensions.height + 10));
						JPen.stroke;
					};
				};
			});
	}
	markerUpdate { |x|
		if(markers.indexOf(x).notNil){
			markers.removeAt(markers.indexOf(x));
		}{
			markers = markers.add(x);	
		};
		values = markers * (1 / dimensions.width);
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