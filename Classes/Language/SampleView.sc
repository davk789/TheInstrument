SampleView {
	var buffer, parent, <>bounds, displayResolution, display, containerView, >action, >mouseUpAction,
	    bufferValue, visibleValue, zoomMin=0, zoomMax=1;
	*new { |par,bnd|
		^super.new.init_sampleview(par,bnd);
	}
	
	init_sampleview { |par,bnd,buf|
		parent = par;
		bounds = bounds ?? { Rect.new(0, 0, 600, 200) };
		buffer = buf;
		displayResolution = bounds.width;
		action = { |obj| postln("default action " ++ obj.curentvalue) };
		mouseUpAction = { |obj| postln("default mouse up action " ++ obj.currentvalue); };
		containerView = GUI.vLayoutView.new(parent,bounds)
		    .background_(Color.clear);
	}
	
	buffer_ { |buf|
		buffer = buf;
	}

	zoom_ { |bottom, top|
		zoomMin = bottom.max(0);
		zoomMax = top.min(1);
	}
	
	zoom {
		^[zoomMin, zoomMax];
	}

	drawView {
		buffer.loadToFloatArray(action:{ |array, buf|
			defer{
				var displayVal, numChannels, resolution, minVal, maxVal;
				numChannels = buf.numChannels;
				bufferValue = array.unlace(numChannels);
				
				minVal = array.minItem;
				maxVal = array.maxItem;
				
				resolution = bufferValue.first.size / displayResolution;
				
				displayVal = [Array.newClear(displayResolution), Array.newClear(displayResolution)];
				
				this.refreshDisplay(numChannels);

				bufferValue.do{ |channelVal,channel|
					displayResolution.do{ |ind|
						displayVal[channel][ind] = channelVal.blendAt(ind * resolution).linlin(minVal, maxVal, 0, 1);
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
		        .value_(Array.fill(displayResolution, { 0.5 }))
		        .editable_(false)
		        .showIndex_(true)
		        .selectionSize_(2)
		        .startIndex_(0)
		        .action_({ |obj|
					var start,end;
					start = this.zoomToAbs(obj.index / obj.value.size);
					end = this.zoomToAbs((obj.selectionSize + obj.index) / obj.value.size);
					[start, end].postln;
					action.value(this);
					//this.setLoopRange(start, end, obj.currentvalue);
			    })
		        .mouseUpAction_({ |obj| mouseUpAction.value(this) });
		})

	}

}
