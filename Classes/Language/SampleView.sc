
SampleView {
	var buffer, parent, <>bounds, resolution, display, containerView;
	*new { |par,bnd|
		^super.new.init_sampleview(par,bnd);
	}
	
	init_sampleview { |par,bnd,buf|
		parent = par;
		bounds = bounds ?? { Rect.new(0, 0, 600, 200) };
		buffer = buf;
		resolution = bounds.width;
		action = { |obj| postln("default action " ++ obj.curentvalue) };
		mouseUpAction = { |obj| postln("default mouse up action " ++ obj.currentvalue); }
		containerView = GUI.vLayoutView.new(parent,bounds)
		    .background_(Color.clear);
	}
	
	buffer_ { |buf|
		buffer = buf;
	}
	
	action_ {
	
	}

	drawView {
		buffer.loadToFloatArray(action:{ |array, buf|
			defer{
				var displayVal, unlaced, numChannels, zoom, minVal, maxVal;
				numChannels = buf.numChannels;
				unlaced = array.unlace(numChannels);
				
				minVal = array.minItem;
				maxVal = array.maxItem;
				
				zoom = unlaced.first.size / resolution;
				
				displayVal = [Array.newClear(resolution), Array.newClear(resolution)];
				
				this.refreshDisplay(numChannels);

				unlaced.do{ |channelVal,channel|
					resolution.do{ |ind|
						displayVal[channel][ind] = channelVal.blendAt(ind * zoom).linlin(minVal, maxVal, 0, 1);
					}
				};
				
				displayVal.do{ |obj,ind|
					display[ind].value = obj;
				};
			}
		});
	}

	refreshDisplay { |numChannels=1|
		var width, height;

		height = 125 / numChannels;

		if(display.notNil){
			display.do{ |obj,ind|
				obj.remove;
			}
		};
		
		display = Array.fill(numChannels, {
			GUI.multiSliderView.new(containerView, Rect.new(0, 0, 0, height))
		        .background_(Color.grey(0.9))
		        .strokeColor_(Color.blue(0.3))
		        .drawLines_(true)
		        .drawRects_(false)
		        .elasticMode_(1)
		        .value_(Array.fill(resolution, { 0.5 }))
		        .editable_(false)
		        .showIndex_(true)
		        .selectionSize_(2)
		        .startIndex_(0)
		        .action_({ |obj|
					var start,end;
					start = this.zoomToAbs(obj.index / obj.value.size);
					end = this.zoomToAbs((obj.selectionSize + obj.index) / obj.value.size);
					[start, end].postln;
					action.value(obj);
					//this.setLoopRange(start, end, obj.currentvalue);
			    })
		        .mouseUpAction_({ |obj| mouseUpAction.value(obj) });

	}

}
