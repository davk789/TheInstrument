
Mixer {
	var s, <channels, <>win, windowHeight=630, parent,
		<fxGroups, <mixGroup, <masterGroup, <masterSubGroups;
	
	*new { |par|
		^super.new.init_mixer(par);
	}
	
	*loadSynthDefs {
		var server;
		server = Server.default;
		SynthDef.new("monoMixerChannel", { |pan=0, lev=1, auxOutLevel=0, auxOutBus=3, inBus=22, outBus=20|
		    var aSig, aIn;
			aIn = In.ar(inBus, 1).softclip;
		    aSig = Pan2.ar(aIn, pan, lev);
		    Out.ar(outBus, aSig);
			Out.ar(auxOutBus, aIn * auxOutLevel);
		}).load(server);
		SynthDef.new("stereoMixerChannel", { |pan=0, lev=1, auxOutLevel=0, auxOutBus=3, inBus=22, outBus=20|
		    var aSig, aIn;
			aIn = In.ar(inBus, 2).softclip;
		    aSig = Balance2.ar(aIn[0], aIn[1], pan, lev);
		    Out.ar(outBus, aSig.softclip);
			Out.ar(auxOutBus, aIn[0] * auxOutLevel);
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
		this.addStereoChannel("master", 0);
	}
	
	initGUI {
		win = GUI.window.new("Mixer", Rect.new(500.rand, 500.rand, 700, windowHeight)).front;
		win.view.decorator = FlowLayout(win.view.bounds);
	}
	
	addMonoChannel { |name, addTarget=0|
		// this doesn't work... WHAT. THE. FUCK.
		win.bounds = Rect.new(win.bounds.left, win.bounds.top, win.bounds.width + 100, win.bounds.height);
		channels = channels.add(name -> MixerChannel.new(parent, name, addTarget, mixGroup, 1));
		channels[name].makeChannelGUI(win, fxGroups);
	}
	
	addStereoChannel { |name, addTarget=0|
		// this doesn't work... WHAT. THE. FUCK.
		win.bounds = Rect.new(win.bounds.left, win.bounds.top, win.bounds.width + 100, win.bounds.height);
		channels = channels.add(name -> MixerChannel.new(parent, name, addTarget, mixGroup, 2));
		channels[name].makeChannelGUI(win, fxGroups);
	}
}

MixerChannel {
	classvar lastInBus=20, channelWidth=100, channelHeight=565;
	var parent, s, <nodeID, insertList, panSpec, effects, channelName, 
		dbSpec, displayBox, <numChannels=1,
	    params,
		channel, inserts, insertMenus, label, labelButton, 
		faders, panFaderView, panFader, auxFaderView, auxFader, auxOutBusMenu, levelFader;

	*new { |par, name, addTarget, group, channels|
		^super.new.init_mixerChannel(par, name, addTarget, group, channels);
	}
	
	*incrementChannelNumber { |channels|
		lastInBus = lastInBus + channels;
	}

	init_mixerChannel { |par, name, addTarget, group, channels|
		s = Server.default;
		parent = par;
		params = Dictionary[
		    'pan' -> 0, 
			'lev' -> 1,
			'auxOutLevel' -> 0,
			'auxOutBus' -> 3,
			'inBus' -> 22, 
			'outBus' -> 20
		];
		numChannels = channels;
		channelName = name ? "master"; 
		panSpec = 'pan'.asSpec;
		dbSpec = [-60, 24].asSpec;
		nodeID = s.nextNodeID;
		insertList = this.getMixerInserts;
		if(channelName == "master"){
			params['outBus'] = 0; // main output channel
		}{
			params['outBus'] = 20;
		};
		params['inBus'] = lastInBus;
		parent.audioBusRegister = parent.audioBusRegister.add(name -> params['inBus']);
		effects = [nil, nil, nil, nil];
		this.class.incrementChannelNumber(numChannels); // "static" method
		lastInBus = params['inBus'] + numChannels;
		this.startChannel(addTarget, group, channels);
	}

	inBus {
		^params['inBus'];
	}
	
	getMixerInserts {
		var ret;
		ret = Array.new;
		EffectBase.allSubclasses.do{ |obj| 
			ret = ret.add(obj.class.asString.split($_)[1]); 
		};
		ret = ["*none*"] ++ ret;
		ret.postln;
		^ret;
	}
	
	startChannel { |addTarget=0, group=1, channels=1|
		channels.switch(
			1, {
				s.listSendMsg(['s_new', 'monoMixerChannel', nodeID, addTarget, group] ++ params.getPairs);
			},
			2, {
				s.listSendMsg(['s_new', 'stereoMixerChannel', nodeID, addTarget, group] ++ params.getPairs);
			}
		);
	}
	
	stopChannel {
		s.sendMsg('n_free', nodeID);
	}
	
	setVolumeFromSlider { |volume|
		var db;
		if(volume == 0){
			db = -inf;
		}{
			db = dbSpec.map(volume);
		};
		displayBox.value = db;
		this.setVolume(db.dbamp);
	}
	
	setVolumeFromNumberBox { |val|
		var amp;
		amp = dbSpec.unmap(val);
		levelFader.value = amp;
		this.setVolume(amp);
	}
	
	setVolume { |val|
	    params['lev'] = val;
		s.sendMsg('n_set', nodeID, 'lev', params['lev']);
	}
		
	setPan { |pan|
		params['pan'] = panSpec.map(pan);
		s.sendMsg('n_set', nodeID, 'pan', params['pan']);
	}

	setauxOutLevel { |val|
		params['auxOutLevel'] = val;//.dbamp;
		s.sendMsg('n_set', nodeID, 'auxOutLevel', params['auxOutLevel']);
	}
	
	doSynthWindow {
		channelName.interpret.relaunchWindow;
	}
	
	setauxOutBus { |val|
		params['auxOutBus'] = val;
		s.sendMsg('n_set', nodeID, 'auxOutBus', params['auxOutBus']);
	}
	
	makeChannelGUI { |win, groups|

		channel = GUI.vLayoutView.new(win, Rect.new(0, 0, channelWidth, channelHeight))
			.background_(Color.black);
		
		label = GUI.hLayoutView.new(channel, Rect.new(0, 0, channelWidth, 25))
			.background_(Color.white);
		labelButton = GUI.button.new(label, label.bounds)
			.states_([[channelName, Color.black, Color.clear]])
			.action_({ |obj| this.doSynthWindow; });

		inserts = GUI.vLayoutView.new(channel, 155)
			.background_(Color.black.alpha_(0.95));

		insertMenus = Array.fill(4, { |ind|
			var menu;
			GUI.button.new(inserts, Rect.new(0, 0, 0, 8))
				.states_([["", Color.black, Color.white]])
				.action_({ |obj| this.launchWindow(menu, ind, groups[ind]) });
			menu = GUI.popUpMenu.new(inserts, Rect.new(0, 0, 0, 20)) 
				.items_(insertList)
				.stringColor_(Color.white)
				.action_({ |obj| this.launchEffect(obj, ind, groups[ind]); });
//				.action_({ |obj| this.launchFXWindow(obj, ind, groups[ind]); });
		});

		faders = GUI.vLayoutView.new(channel, channelHeight * 0.725)
			.background_(Color.white.alpha_(0.75));

		// forcing the slider to be horizontal
		panFaderView = GUI.hLayoutView.new(faders, Rect.new(0, 0, 0, 30));
		panFader = GUI.slider.new(panFaderView, Rect.new(0, 0, channelWidth, 0)).value_(0.5);
		panFader.action = { |obj|
			this.setPan(obj.value);
		};
		// forcing the slider to be horizontal
		auxFaderView = GUI.hLayoutView.new(faders, Rect.new(0, 0, 0, 30))		    
		    .background_(Color.yellow);
		auxOutBusMenu = GUI.popUpMenu.new(auxFaderView, Rect.new(0, 0, 23, 0))
			.items_(Array.fill(s.options.numOutputBusChannels, { |ind| (ind + 1).asString; }))
			.value_(2)
			.action_({ |obj| this.setauxOutBus(obj.value); });
		auxFader = GUI.slider.new(auxFaderView, Rect.new(0, 0, 73, 0)).value_(0.5)
            .value_(0)
		    .action_({ |obj|
				//this.setauxOutLevel(dbSpec.map(obj.value));
				this.setauxOutLevel(obj.value);
			});
		levelFader = GUI.slider.new(faders, Rect.new(0, 0, 0, 315))
			.knobColor_(Color.new255(50,50,50))
			.background_(Color.blue.alpha_(0.25))
			.value_(dbSpec.unmap(0))
			.action_({ |obj| this.setVolumeFromSlider(obj.value); });

		displayBox = GUI.numberBox.new(faders, Rect.new(0, 0, 0, 25))
			.value_(dbSpec.map(levelFader.value))
			.action_({ |obj| this.setVolumeFromNumberBox(obj.value); });
		
	}
	
	launchEffect { |menu, ind, group|
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
	
	launchWindow { |menu,ind,group|
		if(menu.value > 0){
			effects[ind].makeGUI(menu.item); 
		};
	}
}
/*
*** the group numbers should be <1000, nodes can be >1000/derived from s.sendMsg 
	for now. see if this causes conflicts with voice creation. node management might
	need to be cleaned up later, to avoid node id conflicts with synths using dynamic
	node numbers.
*/                                                              
 
