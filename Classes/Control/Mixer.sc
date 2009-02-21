
Mixer {
	var s, <channels, win, windowHeight=530,
		<fxGroups, <mixGroup, <masterGroup, <masterSubGroups;
	*new {
		^super.new.init_mixer;
	}
	init_mixer {
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
		win = GUI.window.new("Output Mix / Plugins", Rect.new(0, 90, 0, windowHeight));
		//win.view.decorator = FlowLayout(win.view.bounds);
		win.front;
	}
	addMonoChannel { |name, group=1, addTarget=0|
		channels = channels.add(name -> MixerChannel.new(name, addTarget, group, 1));
		channels[name].makeChannelGUI(win, fxGroups);
	}
	addStereoChannel { |name, group=1, addTarget=0|
		channels = channels.add(name -> MixerChannel.new(name, addTarget, group, 2));
		channels[name].makeChannelGUI(win, fxGroups);
	}
}

MixerChannel {
	classvar lastInBus=20, insertList, channelWidth=100, channelHeight=530;
	var s, <nodeID, volumeSpec, panSpec, <inBus, <outBus, effects, channelName;
	*new { |name, addTarget, group, channels|
		insertList = ["<none>", "monoDelay", "ringMod", "compressor", "distortion", 
			"eq", "pitchShift"];
		^super.new.init_mixerChannel(name, addTarget, group, channels);
	}
	init_mixerChannel { |name, addTarget, group, channels|
		s = Server.default;
		volumeSpec = 'amp'.asSpec;
		panSpec = [-1, 1].asSpec;
		nodeID = s.nextNodeID;
		channelName = name;
		if(channelName == "master"){
			outBus = 0;
		}{
			outBus = 20;
		};
		inBus = lastInBus;
		~audioBusRegister = ~audioBusRegister.add(name -> inBus);
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

		channel = GUI.vLayoutView.new(win, Rect.new(win.bounds.width, 0, channelWidth, channelHeight))
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
				.allowsReselection_(true)
				.action_({ |obj| this.launchFXWindow(obj, ind, groups[ind]); });
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
		win.bounds = Rect.new(0, 90, win.bounds.width + channelWidth + 10, channelHeight);
	}
	launchFXWindow { |menu, ind, group|
		menu.items[menu.value].switch(
			"<none>", {
				effects[ind].releaseSynth(ind);
				effects[ind] = nil;
			},
			"monoDelay", {
				if( effects[ind].isKindOf(MonoDelay).not ){
					if(effects[ind].notNil){
						effects[ind].releaseSynth;
						effects[ind] = nil;
					};
					effects[ind] = MonoDelay.new(menu, group, channelName, ind);
					effects[ind].makeGUI("monoDelay");
				};
				if(effects[ind].win.isClosed){
					effects[ind].makeGUI("monoDelay");
				}{
					effects[ind].win.front;
				};
			}, {
				postln("default case does nothing");
			}
		);
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
