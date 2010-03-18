SampleView {
	var currentBuffer, parent, <>bounds, displayStartSample=0, displayResolution, <numChannels=1, display, containerView, <>action, <>mouseUpAction,
	    bufferValue, displayValue, minSampleVal, maxSampleVal, activeGuiChannel=0, <vZoom=1;
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
		// this is be the numeric represenatation of the current sample
		action = { postln("default action " ++ this.curentvalue) };
		mouseUpAction = { postln("default mouse up action " ++ this.currentvalue); }; //the obj arg should be accessible... strange
		containerView = GUI.vLayoutView.new(parent,bounds)
		    .background_(Color.clear);
		this.drawDisplay;
	}
	
	buffer {
		^currentBuffer;
	}
	
	buffer_ { |buf|
		this.setBuffer(buf);
	}
	
	setBuffer { |buf|
		currentBuffer = buf;
		this.setNewBuffer(currentBuffer);
	}
	
	setZoom { |x, y|
		var range, start;
		range = (y - x).abs.min(1);
		displayResolution = (range * bounds.width).floor;
		displayStartSample = min(x, y) * bufferValue[0].size;
		this.updateDisplayValue;
		displayValue.do{ |channelVal, index|
			display[index].value = this.getVZoomForDisplay(displayValue[index]);
		};
	}
	
	zoom_ { |val|
		this.setZoom(val[0], val[1]);
	}
	
	zoom {
		var start, end;
		start = displayStartSample / bufferValue[0].size;
		end = (displayResolution / bounds.width) + start;
		^[start, end];
	}
	
	vZoom_ { |val| 
		vZoom = max(val, 1);
		numChannels.do{ |ind| display[ind].value = this.getVZoomForDisplay(displayValue[ind]); };
	}
	
	getVZoomForDisplay { |val|
		^((val - 0.5) * vZoom) + 0.5;
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
		        	activeGuiChannel = ind;
					this.updateSelection(obj.index, obj.selectionSize);
					action.value(this);
			    })
		        .mouseUpAction_({ |obj| mouseUpAction.value(this) });
		})

	}
	
	updateSelection { |index, selectionSize|
		display.do{ |obj,ind|
			obj.index = index;
			obj.selectionSize = selectionSize;
		};
	}
	
	// wrapper methods for SCMultiSliderView
	
	currentvalue {
		^display[activeGuiChannel].currentvalue;
	}
	
	strokeColor_ { |color|
		display.do{ |msView,ind|
			msView.strokeColor = color;
		}
	}
	
	strokeColor {
		^display[activeGuiChannel].strokeColor;
	}

	background_ { |color|
		display.do{ |msView,ind|
			msView.background = color;
		}
	}
	
	background {
		^display[activeGuiChannel].background;
	}
	
	index_ { |color|
		display.do{ |msView,ind|
			msView.index = color;
		}
	}
	
	index {
		^display[activeGuiChannel].index;
	}

	selectionSize_ { |color|
		display.do{ |msView,ind|
			msView.selectionSize = color;
		}
	}
	
	selectionSize {
		^display[activeGuiChannel].selectionSize;
	}
	
}
