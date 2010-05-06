PresetManager {
	var saveButton, saveField, loadMenu, parent;
	*new { |par|
		^super.new.init_presetmanager(par);
	}
	
	init_presetmanager { |par|
		parent = par;
		postln("I am going to manage presets for all objects too avoid redundant code in teh various classes");	
	}
	
	getGUI { |parent|
		
	}
	
}