MonoDelay {
	var s, win, channelName, groupID, nodeID,
		bus=22, mix=0.5, delayTime=0.1, feedback=0, 
		modBus=23, modAmt=0, modLag=1;
	*new { |menu, group|
		^super.new.init_monoDelay(menu, group);
	}
	init_monoDelay { |menu, group|
		s = Server.default;
		groupID = group;
		nodeID = s.nextNodeID;
		channelName = menu.items[menu.value];
		postln("initializing the Mono Delay");
		this.startSynth;
		this.makeGUI;
	}
	startSynth {
		s.sendMsg('s_new', 'fx_monoDelay', nodeID, groupID, 0, 'bus', bus, 'mix', mix,
			'delayTime', delayTime, 'feedback', feedback, 
			'modBus', modBus, 'modAmt', modAmt, 'modLag', modLag);
	}
	releaseSynth {
		s.sendMsg('n_free', nodeID);
	}
	initGUI {
		win = GUI.window.new(channelName, Rect.new(100, 700, 300, 200));
		win.view.decorator = FlowLayout(win.view.bounds);
		win.front;
	}
	makeGUI {
		if(win.notNil){
			if(win.isClosed)
		};
		if(win.isNil){ 
			this.initGUI; 
		}{
			if(win.isClosed){ 
				win = nil;
				this.initGUI;
			};
		};
		
	}
}
/*
parameters:
|bus=20, mix=1, delayTime=0.1, feedback=0, 
 modBus=20, modAmt=0, modLag=1|
*/
           