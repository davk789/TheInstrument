SampleView {
	var currentBuffer, parent, <>bounds, displayStartSample=0, displayResolution, <numChannels=1, display, containerView, <>action, <>mouseUpAction,
	    bufferValue, displayValue, zoomRange, minSampleVal, maxSampleVal;
	*new { |par,bnd|
		^super.new.init_sampleview(par,bnd);
	}
	
	init_sampleview { |par,bnd,buf|
		parent = par;
		bounds = bnd ?? { Rect.new(0, 0, 600, 200) };
		currentBuffer = buf;
		// the respolution.. this will get smaller asa the view zooms in, max amount is the wosth of the view itself
		displayResolution = bounds.width;
		// the value of the multisliderviews. each bottom-level array will be the same size, change according toi the resolution
		displayValue = Array.fill(numChannels, { Array.fill(displayResolution, {0.5}) });
		// in addition there is bufferValue, which will not be set until a buffer is given to SampleView
		// this of course will be the numeric represenatation of the sample set for looping
		action = { |obj| postln("default action " ++ obj.curentvalue) };
		mouseUpAction = { |obj| postln("default mouse up action " ++ obj.currentvalue); };
		containerView = GUI.vLayoutView.new(parent,bounds)
		    .background_(Color.clear);
		this.drawDisplay;
	}
	
	buffer {
		^currentBuffer;
	}
	
	buffer_ {
		"setter disabled, use SampleView:setActiveBuffer(buffer) instead".warn;
	}
	
	setBuffer { |buf|
		currentBuffer = buf;
		// this is more clear than having the function access the member var yes?
		this.setNewBuffer(currentBuffer);
	}


		
	zoomToUnit { |val| // 0..1 version of zoomtoabs
		var range;
		range = displayValue[0].lastIndex;
//		^((val  * range) / displayResolution) + (currBufDisplayStart / displayResolution);
	}
	
	zoomToAbs { |val| // 0..bufferSize version
		var range;
//		range = currBufDisplayEnd - currBufDisplayStart;
//		^((val  * range) / displayResolution) + (currBufDisplayStart / displayResolution);
	}
	
	zoom { |x, y|
		var range;
		range = (y - x).abs.min(1);
		displayResolution = range * bounds.width;
		displayStartSample = min(x, y) * bufferValue[0].size;
		
		this.updateDisplayValue;
	}
	
	updateDisplayValue {
		displayValue = Array.fill(numChannels, { Array.newClear(displayResolution) });
				
		bufferValue.do{ |channelVal,channel|
			displayResolution.do{ |ind|
				var index;
				index = (ind * displayResolution) + displayStartSample;
				displayValue[channel][ind] = channelVal.blendAt(index).linlin(minSampleVal, maxSampleVal, 0, 1);
			}
		};
	}

	setNewBuffer {  |buffer|
		buffer.loadToFloatArray(action:{ |array, buf|
			defer{
				var resolution, minVal, maxVal;
				
				numChannels = buf.numChannels;
				bufferValue = array.unlace(numChannels);
				
				minSampleVal = array.minItem;
				maxSampleVal = array.maxItem;
				
				resolution = bufferValue.first.size / displayResolution;
				
				this.updateDisplayValue;
				this.drawDisplay(numChannels);
			};
		});
	}

	drawDisplay { |chans=1|
		var width, height;

		height = 125 / chans;

		if(display.notNil){
			display.do{ |obj,ind|
				obj.remove;
			}
		};
				
		display = Array.fill(chans, { |ind|
			GUI.multiSliderView.new(containerView, Rect.new(0, 0, 0, height))
		        .background_(Color.grey(0.9))
		        .strokeColor_(Color.blue(0.3))
		        .drawLines_(true)
		        .drawRects_(false)
		        .elasticMode_(1)
		        .value_(displayValue[ind])
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
	
	// wrapper methods for SCMultiSliderView
	
	currentvalue {
		^display[0].currentvalue;
	}

}
