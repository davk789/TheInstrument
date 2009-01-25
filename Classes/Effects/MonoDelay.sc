MonoDelay {
	var s, <win, inputName, inputNumber, groupID, nodeID,
		bus=20, mix=0.5, delayTime=0.1, feedback=0, 
		modBus=23, modAmt=0, modLag=1;
	*new { |menu, group, name, ind|
		^super.new.init_monoDelay(menu, group, name, ind);
	}
	init_monoDelay { |menu, group, name, ind|
		s = Server.default;
		groupID = group;
		nodeID = s.nextNodeID;
		postln("the monoDelay Node ID is " ++ nodeID);
		inputName = name;
		inputNumber = ind;
		postln("initializing the Mono Delay");
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
		
		mixKnob = EZKnob.new(win, 50 @ 16, "mix", labelWidth:50, numberWidth:50)
			.action_({ |obj| obj.value.postln; })
			.value_(mix)
			.centered_(true)
			.action_({ |obj|
				mix = obj.value;
				s.sendMsg('n_set', nodeID, 'mix', mix);
			})
			.knobView.color_([Color.black, Color.yellow, Color.grey, Color.yellow])
			.step_(0.005);
		timeKnob = EZKnob.new(win, 50 @ 16, "time", labelWidth:50, numberWidth:50)
			.set(spec:[0.001, 8, \exponential].asSpec)
			.action_({ |obj|
				delayTime = obj.value;
				s.sendMsg('n_set', nodeID, 'delayTime', delayTime);
			})
			.value_(delayTime)
			.knobView.color_([Color.black, Color.yellow, Color.grey, Color.yellow])
			.step_(0.005);
		fbKnob = EZKnob.new(win, 50 @ 16, "fbk", labelWidth:50, numberWidth:50)
			.action_({ |obj|
				feedback = obj.value;
				s.sendMsg('n_set', nodeID, 'feedback', feedback);
			})
			.value_(feedback)
			.knobView.color_([Color.black, Color.yellow, Color.grey, Color.yellow])
			.step_(0.005);
		modAmtKnob = EZKnob.new(win, 50 @ 16, "modAmt", labelWidth:50, numberWidth:50)
			.action_({ |obj|
				modAmt = obj.value;
				s.sendMsg('n_set', nodeID, 'modAmt', modAmt);
			})
			.value_(modAmt)
			.knobView.color_([Color.black, Color.red, Color.grey, Color.red])
			.step_(0.005);
		modLagKnob = EZKnob.new(win, 50 @ 16, "modLag", labelWidth:50, numberWidth:50)
			.set(spec:[0.01, 4, \exponential].asSpec)
			.action_({ |obj|
				modLag = obj.value;
				s.sendMsg('n_set', nodeID, 'modLag', modLag);
			})
			.value_(modLag)
			.knobView.color_([Color.black, Color.red, Color.grey, Color.red])
			.step_(0.005);

		win.front;
							
	}
}
/*
parameters:
|bus=20, 
mix=1, delayTime=0.1, feedback=0, modBus=20, modAmt=0, modLag=1|
*/
