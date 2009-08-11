MonoDelay : EffectBase {
	*new { |par, group, name, ind|
		^super.new(par, group, name, ind).init_monoDelay;
	}
	init_monoDelay {
		var bus;
		bus = parent.mixer.channels[inputName].inBus;
		winBounds = Rect.new(winBounds.left, winBounds.top, 400, 200);

		synthdefName = 'fx_monoDelay';
		startParams = Dictionary[
			'bus' -> bus, 
			'mix' -> 0.5,
			'delayTime' -> 0.1, 
			'feedback' -> 0, 
			'modBus' -> bus, 
			'modAmt' -> 0, 
			'modLag' -> 1
		];
		// initialization functions
		this.startSynth;
		this.initGUI;
		
	}
	
	setModBus { |obj|
		startParams['modBus'] = parent.audioBusRegister[obj.item];
		server.sendMsg('n_set', nodeID, 'modBus', startParams['modBus']);
	}
	
	setMix { |val|
		startParams['mix'] = val;
		server.sendMsg('n_set', nodeID, 'mix', startParams['mix']);
	}
	
	setDelayTime { |val|
		startParams['delayTime'] = val;
		server.sendMsg('n_set', nodeID, 'delayTime', startParams['delayTime']);
	}
	
	setFeedback { |val|
		startParams['feedback'] = val;
		server.sendMsg('n_set', nodeID, 'feedback', startParams['feedback']);
	}
	
	setModAmt { |val|
		startParams['modAmt'] = val;
		server.sendMsg('n_set', nodeID, 'modAmt', startParams['modAmt']);
	}
	
	setModLag { |val|
		startParams['modLag'] = val;
		server.sendMsg('n_set', nodeID, 'modLag', startParams['modLag']);
	}

	makeGUI { |name|
		var labelRow, modSourceMenu, mixKnob, timeKnob, fbKnob, modAmtKnob, modLagKnob;
/*		if(win.isClosed){ 
			win = nil;
			this.initGUI;
		};
*/		labelRow = GUI.hLayoutView.new(win, Rect.new(0, 0, win.view.bounds.width, 22))			.background_(Color.blue(0.2, alpha:0.1));
		GUI.staticText.new(labelRow, Rect.new(0, 0, labelRow.bounds.width / 2.1, 0))
			.string_(inputName ++ " channel, slot " ++ inputNumber)
			.stringColor_(Color.white);
		modSourceMenu = GUI.popUpMenu.new(labelRow, Rect.new(0, 0, labelRow.bounds.width / 2.1, 0))
			.items_(parent.audioBusRegister.keys.asArray)
			.action_({ |obj| this.setModBus(obj); })
			.stringColor_(Color.white);
		modSourceMenu.value = modSourceMenu.items.indexOf(parent.audioBusRegister.findKeyForValue(startParams['modBus']));

		mixKnob = EZJKnob.new(win, Rect.new(0, 0, 50, 100), "mix")
			.value_(startParams['mix'])
			.knobAction_({ |val| this.setMix(val); })
			.knobColor_([Color.black, Color.yellow, Color.grey, Color.yellow])
			.stringColor_(Color.white)
			.knob.step_(0.005);
		timeKnob = EZJKnob.new(win, Rect.new(0, 0, 50, 100), "time")
			.knobAction_({ |val| this.setDelayTime(val); })
			.value_(startParams['delayTime'])
			.spec_([0.001, 8, \exponential].asSpec)
			.knobColor_([Color.black, Color.yellow, Color.grey, Color.yellow])
			.stringColor_(Color.white)
			.knob.step_(0.005);
		fbKnob = EZJKnob.new(win, Rect.new(0, 0, 50, 100), "fbk")
			.knobAction_({ |val| this.setFeedback(val); })
			.value_(startParams['feedback'])
			.knobColor_([Color.black, Color.yellow, Color.grey, Color.yellow])
			.stringColor_(Color.white)
			.knob.step_(0.005);
		modAmtKnob = EZJKnob.new(win, Rect.new(0, 0, 50, 100), "modAmt")
			.knobAction_({ |val| this.setModAmt(val); })
			.value_(startParams['modAmt'])
			.knobColor_([Color.black, Color.red, Color.grey, Color.red])
			.stringColor_(Color.white)
			.knob.step_(0.005);
		modLagKnob = EZJKnob.new(win, Rect.new(0, 0, 50, 100), "modLag")
			.knobAction_({ |val| this.setModLag(val); })
			.value_(startParams['modLag'])
			.spec_([0.01, 4, \exponential].asSpec)
			.knobColor_([Color.black, Color.red, Color.grey, Color.red])
			.stringColor_(Color.white)
			.knob.step_(0.005);

		win.front;
							
	}
	
	*loadSynthDef { |s|
		s = s ? Server.default;
		SynthDef.new("fx_monoDelay", { |bus=20, mix=1, delayTime=0.1, feedback=0, 
								modBus=20, modAmt=0, modLag=1|
		    var aIn, aDelay, aDelayIn, aLocalIn, aOutMix, aModIn;
		    aModIn = Lag.ar(InFeedback.ar(modBus) * modAmt, modLag);
			aLocalIn = LocalIn.ar(1);
		    aIn = In.ar(bus);
			aDelayIn = (aIn * (feedback - 1).abs) + (aLocalIn * feedback);
		    aDelay = DelayC.ar(aDelayIn, 10, delayTime + aModIn);
			LocalOut.ar(aDelay);
			aOutMix = (aIn * (mix - 1).abs) + (aDelay * mix);
		    ReplaceOut.ar(bus, aOutMix.softclip);
		}).load(s);

	}
}
/*
parameters:
|bus=20, 
mix=1, delayTime=0.1, feedback=0, modBus=20, modAmt=0, modLag=1|
*/
                    
