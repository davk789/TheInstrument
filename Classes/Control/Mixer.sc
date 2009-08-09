
Mixer {
	var s, <channels, <>win, windowHeight=560, parent,
		<fxGroups, <mixGroup, <masterGroup, <masterSubGroups;
	
	*new { |par|
		^super.new.init_mixer(par);
	}
	
	*loadSynthDefs { |server|
		server = server ? Server.default;
		SynthDef.new("monoMixerChannel", { |pan=0, lev=1, gain=1, inBus=22, outBus=20|
		    var aSig, aIn;
			aIn = (In.ar(inBus, 1) * gain).softclip;
		    aSig = Pan2.ar(aIn, pan, lev);
		    Out.ar(outBus, aSig);
			Out.ar(3, aSig); // 
		}).load(server);
		SynthDef.new("monoMixerChannelNoAuxOut", { |pan=0, lev=1, gain=1, inBus=22, outBus=20|
		    var aSig, aIn;
			aIn = (In.ar(inBus, 1) * gain).softclip;
		    aSig = Pan2.ar(aIn, pan, lev);
		    Out.ar(outBus, aSig);
		}).load(server);
		SynthDef.new("stereoMixerChannel", { |pan=0, lev=1, gain=1, inBus=22, outBus=20|
		    var aSig, aIn;
			aIn = (In.ar(inBus, 2) * gain).softclip;
		    aSig = Balance2.ar(aIn[0], aIn[1], pan, lev);
		    Out.ar(outBus, aSig.softclip);
		}).load(server);
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
		this.addStereoChannel("master", 0, true);
	}
	
	//// GUI methods
	initGUI {
		win = GUI.window.new("Mixer", Rect.new(500.rand, 500.rand, 700, windowHeight)).front;
		win.view.background = Color.grey(15);
		win.view.decorator = FlowLayout(win.view.bounds);
	}
	
	addMonoChannel { |name, addTarget=0, noAux=false|
		// this doesn't work... WHAT. THE. FUCK.
		win.bounds = Rect.new(win.bounds.left, win.bounds.top, win.bounds.width + 100, win.bounds.height);
		channels = channels.add(name -> MixerChannel.new(parent, name, addTarget, mixGroup, 1, noAux));
		channels[name].makeChannelGUI(win, fxGroups);
	}
	
	addStereoChannel { |name, addTarget=0, noAux=false|
		// this doesn't work... WHAT. THE. FUCK.
		win.bounds = Rect.new(win.bounds.left, win.bounds.top, win.bounds.width + 100, win.bounds.height);
		channels = channels.add(name -> MixerChannel.new(parent, name, addTarget, mixGroup, 2, noAux));
		channels[name].makeChannelGUI(win, fxGroups);
	}
}

MixerChannel {
	classvar lastInBus=20, insertList, channelWidth=100, channelHeight=530;
	var parent, s, <nodeID, volumeSpec, panSpec, <inBus, <outBus, effects, channelName, parent;

	*new { |par, name, addTarget, group, channels, noAux=false|
		insertList = ["*none*", "MonoDelay", "Distortion", "Compressor", "RingMod", 
			"EQ", "PitchShift"];
		^super.new.init_mixerChannel(par, name, addTarget, group, channels, noAux);
	}

	init_mixerChannel { |par, name, addTarget, group, channels, noAux=false|
		s = Server.default;
		parent = par;
		channelName = name ? "master"; 
		volumeSpec = 'amp'.asSpec;
		panSpec = [-1, 1].asSpec;
		nodeID = s.nextNodeID;
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
				.stringColor_(Color.white)
				.action_({ |obj| this.launchFXWindow(obj, ind, groups[ind]); });
		});
		Platform.case('linux', {
			insertMenus.do{ |obj,ind|
				obj.allowsReselection_(true)
			};
		});
		faders = GUI.vLayoutView.new(channel, channelHeight * 0.725)
			.background_(Color.white.alpha_(0.75));

		// forcing the slider to be horizontal
		panFaderView = GUI.hLayoutView.new(faders, Rect.new(0, 0, 0, 30));
		panFader = GUI.slider.new(panFaderView, Rect.new(0, 0, channelWidth, 0)).value_(0.5);
		panFader.action = { |obj|
			this.setPan(obj.value);
		};		
		levelFader = GUI.slider.new(faders, Rect.new(0, 0, 0, 315))
			.knobColor_(Color.new255(50,50,50))
			.background_(Color.blue.alpha_(0.25))
			.value_(0.75);
		levelFader.action = { |obj|
			this.setVolume(obj.value);
		};
	}
	
	launchFXWindow { |menu, ind, group|
		if(menu.value > 0){
			if(effects[ind].notNil){
				effects[ind].releaseSynth;
				effects[ind] = nil;
			};
			effects[ind] = menu.item.interpret.new(parent, group, channelName, ind);
			effects[ind].makeGUI(menu.item);
		}{
			effects[ind].releaseSynth(ind);
			effects[ind] = nil;
		};
	}
}
/*
*** the group numbers should be <1000, nodes can be >1000/derived from s.sendMsg 
	for now. see if this causes conflicts with voice creation. node management might
	need to be cleaned up later, to avoid node id conflicts with synths using dynamic
	node numbers.
*/                                                              
 
