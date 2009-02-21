MonoDelay {
	var s, <win, inputName, inputNumber, groupID, <nodeID,
		bus=20, mix=0.5, delayTime=0.1, feedback=0, 
		modBus=23, modAmt=0, modLag=1;
	*new { |menu, group, name, ind|
		^super.new.init_monoDelay(menu, group, name, ind);
	}
	init_monoDelay { |menu, group, name, ind|
		s = Server.default;
		groupID = group;
		nodeID = s.nextNodeID;
		inputName = name;
		inputNumber = ind;
		bus = ~mixer.channels[name].inBus;
		modBus = bus;
		postln("initializing the Mono Delay with node ID " ++ nodeID);
		this.startSynth;
		this.initGUI;
		
	}
	startSynth {
		s.sendMsg('s_new', 'fx_monoDelay', nodeID, 0, groupID, 'bus', bus, 'mix', mix,
			'delayTime', delayTime, 'feedback', feedback, 
			'modBus', modBus, 'modAmt', modAmt, 'modLag', modLag);
	}
	releaseSynth {
		s.sendMsg('n_free', nodeID);
	}
	initGUI {
		win = GUI.window.new("mono delay", Rect.new(100, 700, 300, 150));
		win.view.decorator = FlowLayout(win.view.bounds);
	}
	makeGUI { |name|
		var labelRow, modSourceMenu, mixKnob, timeKnob, fbKnob, modAmtKnob, modLagKnob;
		if(win.isClosed){ 
			win = nil;
			this.initGUI;
		};
		labelRow = GUI.hLayoutView.new(win, Rect.new(0, 0, win.view.bounds.width, 22))			.background_(Color.blue(0.2, alpha:0.1));
		GUI.staticText.new(labelRow, Rect.new(0, 0, labelRow.bounds.width / 2.1, 0))
			.string_(inputName ++ " channel, slot " ++ inputNumber);
		modSourceMenu = GUI.popUpMenu.new(labelRow, Rect.new(0, 0, labelRow.bounds.width / 2.1, 0))
			.items_(~audioBusRegister.keys.asArray)
			.action_({ |obj|
				modBus = ~audioBusRegister[obj.items[obj.value]];
				s.sendMsg('n_set', nodeID, 'modBus', modBus);
			});
		modSourceMenu.value = modSourceMenu.items.indexOf(~audioBusRegister.findKeyForValue(modBus));

		mixKnob = EZJKnob.new(win, Rect.new(0, 0, 50, 100), "mix")
			.value_(mix)
			.knobAction_({ |val|
				mix = val;
				s.sendMsg('n_set', nodeID, 'mix', mix);
			})
			.knobColor_([Color.black, Color.yellow, Color.grey, Color.yellow])
			.knob.step_(0.005);
		timeKnob = EZJKnob.new(win, Rect.new(0, 0, 50, 100), "time")
			.knobAction_({ |val|
				delayTime = val;
				s.sendMsg('n_set', nodeID, 'delayTime', delayTime);
			})
			.value_(delayTime)
			.spec_([0.001, 8, \exponential].asSpec)
			.knobColor_([Color.black, Color.yellow, Color.grey, Color.yellow])
			.knob.step_(0.005);
		fbKnob = EZJKnob.new(win, Rect.new(0, 0, 50, 100), "fbk")
			.knobAction_({ |val|
				feedback = val;
				s.sendMsg('n_set', nodeID, 'feedback', feedback);
			})
			.value_(feedback)
			.knobColor_([Color.black, Color.yellow, Color.grey, Color.yellow])
			.knob.step_(0.005);
		modAmtKnob = EZJKnob.new(win, Rect.new(0, 0, 50, 100), "modAmt")
			.knobAction_({ |val|
				modAmt = val;
				s.sendMsg('n_set', nodeID, 'modAmt', modAmt);
			})
			.value_(modAmt)
			.knobColor_([Color.black, Color.red, Color.grey, Color.red])
			.knob.step_(0.005);
		modLagKnob = EZJKnob.new(win, Rect.new(0, 0, 50, 100), "modLag")
			.knobAction_({ |val|
				modLag = val;
				s.sendMsg('n_set', nodeID, 'modLag', modLag);
			})
			.value_(modLag)
			.spec_([0.01, 4, \exponential].asSpec)
			.knobColor_([Color.black, Color.red, Color.grey, Color.red])
			.knob.step_(0.005);

		win.front;
							
	}
}
/*
parameters:
|bus=20, 
mix=1, delayTime=0.1, feedback=0, modBus=20, modAmt=0, modLag=1|
*/
                    