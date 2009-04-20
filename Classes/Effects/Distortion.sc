Distortion {
	var server, inputName, inputNumber, groupID, <nodeID, bus, chebyAmps, drawFunction,
		expPreBuffer, chebyPreBuffer, postBuffer, expArr, chebyArr, postArr, postSignal, chebyAmt=0, expAmt=1, tableSize=1024,
		postMixBuffer,
		<win, shapeView, curve=1, mix, gain=1;
	*new { |group,name,ind|
		^super.new.init_distortion(group, name, ind);
	}
	init_distortion { |group,name,ind|
		server = Server.default;
		nodeID = server.nextNodeID;
		groupID = group;
		inputName = name;
		inputNumber = ind;
		mix = -1;
		chebyAmps = [1,0,0,0, 0,0,0,0, 0,0,0,0, 0,0,0,0, 0,0,0];
		bus = ~mixer.channels[inputName].inBus;
		chebyArr = Array.new;
		expArr = this.getExpoCurve(tableSize);
		postArr = Array.new;
		chebyPreBuffer = Buffer.alloc(server, tableSize, 1);
		expPreBuffer  = Buffer.alloc(server, tableSize, 1);
		postMixBuffer  = Buffer.alloc(server, tableSize * 2, 1);
		postSignal = Signal.newClear(tableSize);
		drawFunction = {
			var displayY=0, displayX=0;
			JPen.use{
				JPen.width = 1;
				
				JPen.color = Color.white;
				JPen.moveTo(0 @ 125);
				JPen.lineTo(250 @ 125);
				JPen.moveTo(125 @ 0);
				JPen.lineTo(125 @ 250);
				JPen.stroke;
				
				JPen.color = Color.yellow;
				JPen.moveTo(0 @ 250);
				postArr.do { |val,ind|
					displayX = ind * 0.244140625;
					displayY = 250 - ((val + 1) * 125);
					JPen.lineTo(displayX @ displayY);
				};
				JPen.stroke;
			};
		};
		this.writeBuffers;
		this.startSynth;
		this.initGUI;
		//this.makeGUI;
	}
	startSynth {
		server.sendMsg('s_new', 'fx_distortion', nodeID, 0, groupID,
			'buffer', postMixBuffer.bufnum, 'gain', gain, 'mix', mix, 'bus', bus);
	}
	releaseSynth {
		server.sendMsg('n_free', nodeID);
	}
	writeBuffers {
		var task;
		
		task = Task.new({
			chebyPreBuffer.cheby(chebyAmps, true, false, true);
			0.2.wait;
			expArr = this.getExpoCurve(tableSize);
			chebyPreBuffer.loadToFloatArray(action:{ |arr| 
				chebyArr = arr;
				expPreBuffer.loadCollection(expArr, action:{ |buf|
					postArr = ((chebyArr * chebyAmt) + (expArr * expAmt)).clip2(1);
					postSignal.waveFill({ |xval,ind|
						postArr[xval];
					}, 0, tableSize);
					postMixBuffer.loadCollection(postSignal.asWavetable);
				});
			});
			0.1.wait;
			shapeView.refresh;
		}, AppClock);
		task.reset;
		task.play;

	}
	calculateExponent { |index,numPoints|
		var unscaledLinY=(-1), unscaledCurveY=(-1);
		unscaledLinY = (index / (numPoints / 2)) - 1;
		if(unscaledLinY.isPositive){
			unscaledCurveY = (unscaledLinY).pow(curve);
		}{
			unscaledCurveY = (unscaledLinY.abs).pow(curve).neg;
		};
		^unscaledCurveY;
	}
	getExpoCurve { |size|
		var arr;
		arr =  Array.fill(size, { |ind|
			this.calculateExponent(ind,size);
		});
		arr = arr.rotate((size / 2).asInt);
		arr = arr.collect{ |obj,ind|
			var ret;
			if(obj.isPositive){
				ret = obj - 1;
			}{
				ret = obj + 1;
			};
			ret;
		};
		^arr;
	}
	setCurve { |val|
		curve = val;
		this.writeBuffers;
	}
	setChebyAmt { |val|
		chebyAmt = val;
		this.writeBuffers;
	}
	setChebyAmps { |val|
		chebyAmps = val;
		this.writeBuffers;
	}
	setExpAmt { |val|
		expAmt = val;
		this.writeBuffers;
	}
	setWetDryMix { |val|
		mix = val;
		server.sendMsg('n_set', nodeID, 'mix', mix);
	}
	setGain { |val|
		gain = val;
		server.sendMsg('n_set', nodeID, 'gain', gain);
	}
	initGUI {
		win = GUI.window.new("Distortion", Rect.new(100, 350, 550, 280))
			.front;
		win.view
			.decorator_(FlowLayout(win.view.bounds))
			.background_(Color.black);
	}
	makeGUI {
		var expCurveSlider, expAmtSlider, chebyAmtSlider, chebyCoefSlider, sliderColumn, labelColumn, mixSlider, gainSlider;
		if(win.isClosed){ 
			win = nil;
			this.initGUI;
		};
		GUI.staticText.new(win, Rect.new(0, 0, 540, 15))
			.align_('center')
			.string_(inputName ++ " channel, slot " ++ inputNumber)
			.stringColor_(Color.yellow);
		shapeView = GUI.userView.new(win, Rect.new(0, 0, 250, 250))
			.relativeOrigin_(true)
			.background_(Color.black.alpha_(0.8))
			//.mouseUpAction_({ |obj,x,y,mod| this.refreshUserView(x,y,mod); })
			.drawFunc_(drawFunction);
		sliderColumn = GUI.vLayoutView.new(win, Rect.new(0, 0, 200, 250))
			.background_(Color.black);
		chebyCoefSlider = GUI.multiSliderView.new(sliderColumn, Rect.new(0, 0, 0, 100))
			.fillColor_(Color.black)
			.strokeColor_(Color.yellow)
			.indexThumbSize_(9.4)
			.background_(Color.black.alpha_(0.9))
			.valueThumbSize_(3.4)
			.value_(chebyAmps)
			.isFilled_(true)
			.mouseUpAction_({ |obj| this.setChebyAmps(obj.value); });

		chebyAmtSlider = GUI.hLayoutView.new(sliderColumn, Rect.new(0, 0, 0, 25));
		GUI.slider.new(chebyAmtSlider, Rect.new(0, 0, 200, 0))
			.value_(chebyAmt)
			.mouseUpAction_({ |obj| this.setChebyAmt(obj.value); });

		expCurveSlider = GUI.hLayoutView.new(sliderColumn, Rect.new(0, 0, 0, 25));
		GUI.slider.new(expCurveSlider, Rect.new(0, 0, 200, 0))
			.value_([1, 0.001, 0.5].asSpec.unmap(curve))
			.mouseUpAction_({ |obj| this.setCurve([0.01, 10, 0.5].asSpec.map(obj.value)) });
		
		expAmtSlider = GUI.hLayoutView.new(sliderColumn, Rect.new(0, 0, 0, 25));
		GUI.slider.new(expAmtSlider, Rect.new(0, 0, 200, 0))
			.value_(expAmt)
			.mouseUpAction_({ |obj| this.setExpAmt(obj.value); }); 

		gainSlider = GUI.hLayoutView.new(sliderColumn, Rect.new(0, 0, 0, 25));
		GUI.slider.new(gainSlider, Rect.new(0, 0, 200, 0))
			.value_([0.001, 4, 2].asSpec.unmap(gain))
			.action_({ |obj| this.setGain([0.001, 4, 2].asSpec.map(obj.value)); }); 

		mixSlider = GUI.hLayoutView.new(sliderColumn, Rect.new(0, 0, 0, 25));
		GUI.slider.new(mixSlider, Rect.new(0, 0, 200, 0))
			.value_('pan'.asSpec.unmap(mix))
			.action_({ |obj| this.setWetDryMix('pan'.asSpec.map(obj.value)); }); 


		labelColumn = GUI.vLayoutView.new(win, Rect.new(0, 0, 75, 250))
			.background_(Color.black);
		GUI.staticText.new(labelColumn, Rect.new(0, 0, 0, 100))
			.stringColor_(Color.yellow)
			.string_("cheby partials");
		GUI.staticText.new(labelColumn, Rect.new(0, 0, 0, 25))
			.stringColor_(Color.yellow)
			.string_("cheby amt");
		GUI.staticText.new(labelColumn, Rect.new(0, 0, 0, 25))
			.stringColor_(Color.yellow)
			.string_("exp coef");
		GUI.staticText.new(labelColumn, Rect.new(0, 0, 0, 25))
			.stringColor_(Color.yellow)
			.string_("exp amt");		
		GUI.staticText.new(labelColumn, Rect.new(0, 0, 0, 25))
			.stringColor_(Color.yellow)
			.string_("gain");
		GUI.staticText.new(labelColumn, Rect.new(0, 0, 0, 25))
			.stringColor_(Color.yellow)
			.string_("mix");

	}
}


  