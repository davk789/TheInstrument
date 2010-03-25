SampleView {
	var <currentBuffer, parent, <>bounds, zoomStart=0, zoomRange=1,
		<displayResolution, <numChannels=1, display, containerView, 
		<>action, <>mouseUpAction, <>mouseDownAction, <>mouseMoveAction, <>mouseOverAction,
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
		action = { postln("default action "/* ++ this.curentvalue*/) };
		mouseUpAction = { postln("default mouse up action "/* ++ this.currentvalue*/); }; 
		mouseDownAction = { postln("default mouse up action "/* ++ this.currentvalue*/); }; 
		mouseOverAction = { postln("default mouse over action "/* ++ this.currentvalue*/); }; 
		mouseMoveAction = { postln("default mouse move action "/* ++ this.currentvalue*/); }; 
		
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
		start = min(x, y);
		zoomStart = start;
		zoomRange = range;
		this.updateDisplayValue;
		displayValue.do{ |channelVal, index|
			display[index].value = this.getVZoomValue(channelVal);
			//display[index].value = (channelVal * vZoom).linlin(minSampleVal, maxSampleVal, 0, 1);
		};
	}
	
	zoom_ { |val|
		this.setZoom(val[0], val[1]);
	}
	
	zoom {
		// returns start and end points of the visible display area
		^[zoomStart, zoomRange + zoomStart];
	}
	
	vZoom_ { |val|
		vZoom = max(val, 1);
		this.updateDisplayValue;
		numChannels.do{ |ind|
			display[ind].value = this.getVZoomValue(displayValue[ind]);
		};
	}
	
	getVZoomValue { |val|
		^((val - 0.5) * vZoom) + 0.5;
	}
	
	updateDisplayValue {
		displayValue = Array.fill(numChannels, { Array.newClear(displayResolution) });
		bufferValue.do{ |channelVal,channel|
			displayResolution.do{ |ind|
				var index;
				index = (ind * (channelVal.size / displayResolution) * zoomRange) + (zoomStart * channelVal.size);
				displayValue[channel][ind] = channelVal.blendAt(index).linlin(minSampleVal,maxSampleVal, 0, 1);
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
		        .value_(this.getVZoomValue(displayValue[ind]))
		        .editable_(false)
		        .showIndex_(true)
		        .selectionSize_(2)
		        .startIndex_(0)
		        .action_({ |obj|
		        	activeGuiChannel = ind;
					this.updateSelection(obj.index, obj.selectionSize);
					action.value(this);
			    })
		        .mouseUpAction_({ |obj| mouseUpAction.value(this) })
		        .mouseDownAction_({ |obj| mouseDownAction.value(this) })
		        .mouseOverAction_({ |obj| mouseOverAction.value(this) })
		        .mouseMoveAction_({ |obj| mouseMoveAction.value(this) });
		        
		})

	}
	
	updateSelection { |index, selectionSize|
		display.do{ |obj,ind|
			obj.index = index;
			obj.selectionSize = selectionSize;
		};
	}
	
	sampleIndex {
		var displayRatio;
		displayRatio = currentBuffer.numFrames / displayResolution;
		^(display[activeGuiChannel].index * zoomRange * displayRatio) + (zoomStart * currentBuffer.numFrames);
	}
	
	sampleSelectionSize {
		var displayRatio = currentBuffer.numFrames / displayResolution;
		^display[activeGuiChannel].selectionSize * displayRatio * zoomRange;
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
	
	// these methods refer to the gui element and will get and set erroneous data when the view is zoomed in
	
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
