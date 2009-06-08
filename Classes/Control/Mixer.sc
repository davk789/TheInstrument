
Mixer {
	var s, <channels, win, windowHeight=530, parent,
		<fxGroups, <mixGroup, <masterGroup, <masterSubGroups;
	*new { |par|
		^super.new.init_mixer(par);
	}
	init_mixer { |par|
		parent = par;
		s = Server.default;
		s.sendMsg('g_new', 500, 1, 1);
		channels = Dictionary.new;
		fxGroups = Array.fill(4, { |ind| ind + 900; });
		mixGroup = fxGroups.last + 1;
		masterGroup = mixGroup + 1;
		masterSubGroups = Array.fill(4, { |ind| ind + masterGroup + 1;});
		4.do{ |ind|
			s.sendMsg('g_new', fxGroups[ind], 1, 500);
		};
		s.sendMsg('g_new', mixGroup, 1, 500);
		s.sendMsg('g_new', masterGroup, 1, 500);
		4.do{ |ind|
			s.sendMsg('g_new', masterSubGroups[ind], 1, masterGroup);
		};
		this.initGUI;
		this.addStereoChannel("master", masterGroup, 1);
	}
	//// GUI methods
	initGUI {
		win = GUI.window.new("Output Mix / Plugins", Rect.new(500.rand, 500.rand, 555, windowHeight)).front;
		win.view.background = Color.grey(15);
		win.view.decorator = FlowLayout(win.view.bounds);
	}
	addMonoChannel { |name, group=1, addTarget=0, noAux=false|
		channels = channels.add(name -> MixerChannel.new(parent, name, addTarget, group, 1, noAux));
		channels[name].makeChannelGUI(win, fxGroups);
	}
	addStereoChannel { |name, group=1, addTarget=0, noAux=false|
		channels = channels.add(name -> MixerChannel.new(parent, name, addTarget, group, 2, noAux));
		channels[name].makeChannelGUI(win, fxGroups);
	}
}

MixerChannel {
	classvar lastInBus=20, insertList, channelWidth=100, channelHeight=530;
	var parent, s, <nodeID, volumeSpec, panSpec, <inBus, <outBus, effects, channelName, synthName, parent;
	*new { |par, name, addTarget, group, channels, noAux=false|
		insertList = ["<none>", "MonoDelay", "Distortion", "Compressor", "RingMod", 
			"EQ", "PitchShift"];
		^super.new.init_mixerChannel(par, name, addTarget, group, channels, noAux);
	}
	init_mixerChannel { |par, name, addTarget, group, channels, noAux=false|
		s = Server.default;
		parent = par;
		if(noAux){
			synthName = 's_monoMixerChannelNoAuxOut'
		}{
			synthName = 's_monoMixerChannel'
		};
		volumeSpec = 'amp'.asSpec;
		panSpec = [-1, 1].asSpec;
		nodeID = s.nextNodeID;
		channelName = name;
		if(channelName == "master"){
			outBus = 0; // MAIN OUTPUT CHANNEL
		}{
			outBus = 20;
		};
		inBus = lastInBus;
		parent.audioBusRegister = parent.audioBusRegister.add(name -> inBus);
		effects = [nil, nil, nil, nil];
		lastInBus = inBus + channels;
		this.startChannel(addTarget, group, channels);
	}
	startChannel { |addTarget=0, group=1, channels=1|
		channels.switch(
			1, {
				s.sendMsg('s_new', 'monoMixerChannel', nodeID, addTarget, group, 'inBus', inBus, 'outBus', outBus);
			},
			2, {
				s.sendMsg('s_new', 'stereoMixerChannel', nodeID, addTarget, group, 'inBus', inBus, 'outBus', outBus);
			}
		);
	}
	stopChannel {
		s.sendMsg('n_free', nodeID);
	}
	setVolume { |volume|
		s.sendMsg('n_set', nodeID, 'lev', volumeSpec.map(volume));
	}
	setPan { |pan|
		s.sendMsg('n_set', nodeID, 'pan', panSpec.map(pan));
	}
	makeChannelGUI { |win, groups|
		var channel, inserts, insertMenus, label, labelText, 
		faders, panFaderView, panFader, levelFader;
		win.bounds = Rect.new(win.bounds.left, win.bounds.top, win.bounds.width + channelWidth + 10, channelHeight);

		channel = GUI.vLayoutView.new(win, Rect.new(0, 0, channelWidth, channelHeight))
			.background_(Color.red);
		
		label = GUI.hLayoutView.new(channel, Rect.new(0, 0, channelWidth, 25))
			.background_(Color.white);
		labelText = GUI.staticText.new(label, label.bounds)
			.string_(channelName);

		inserts = GUI.vLayoutView.new(channel, channelHeight * 0.275)
			.background_(Color.black.alpha_(0.95));

		insertMenus = Array.fill(4, { |ind|
			GUI.popUpMenu.new(inserts, Rect.new(0, 0, 0, 30))
				.items_(insertList)
				.action_({ |obj| this.launchFXWindow(obj, ind, groups[ind]); });
		});
		Platform.case('linux', {
			insertMenus.do{ |obj,ind|
				obj.allowsReselection_(true)
			};
		});
		faders = GUI.vLayoutView.new(channel, channelHeight * 0.725)
			.background_(Color.white.alpha_(0.95));

		// forcing the slider to be horizontal
		panFaderView = GUI.hLayoutView.new(faders, Rect.new(0, 0, 0, 30));
		panFader = GUI.slider.new(panFaderView, Rect.new(0, 0, channelWidth, 0)).value_(0.5);
		panFader.action = { |obj|
			this.setPan(obj.value);
		};		
		levelFader = GUI.slider.new(faders, Rect.new(0, 0, 0, (channelHeight * 0.725) - 45)).value_(0.75);
		levelFader.action = { |obj|
			this.setVolume(obj.value);
		};
	}
	launchFXWindow { |menu, ind, group|
		if(menu.value > 0){
				if( effects[ind].isKindOf(menu.item.interpret).not ){
					if(effects[ind].notNil){
						effects[ind].releaseSynth;
						effects[ind] = nil;
					};
					effects[ind] = menu.item.interpret.new(parent, group, channelName, ind);
					effects[ind].makeGUI(menu.item);
				};
				if(effects[ind].win.isClosed){
					effects[ind].makeGUI(menu.item);
				}{
					effects[ind].win.front;
				};
		}{
				effects[ind].releaseSynth(ind);
				effects[ind] = nil;
		};
	}
}
/*
this is how things look on startup now:
NODE TREE Group 0
   1 group
      400 group
         999 group
         1003 group
            1002 r_klank
         1005 group
            1004 r_lpf
         1007 group
            1006 r_hpf
         1009 group
            1008 r_hpf
         1011 group
            1010 r_klank
         1013 group
            1012 r_klank
         1015 group
            1014 r_klank
         1017 group
            1016 r_lpf
      102 group
      103 group
      500 group
         900 group
         901 group
         902 group
         903 group
         904 group
            1026 monoMixerChannel
            1001 monoMixerChannel
         905 group
            906 group
            907 group
            908 group
            909 group
            1000 stereoMixerChannel

*** the group numbers should be <1000, nodes can be >1000/dereived from s.sendMsg 
	for now. see if this causes conflicts with voice creation. node management might
	need to be cleaned up later, to avoid mode id conflicts with synths using dynamic
	node numbers.
*/                                                              
 
