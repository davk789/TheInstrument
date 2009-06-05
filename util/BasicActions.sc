QuickKeyboard { // quick and dirty replacement for my Axiom25 keyboard
	var <>synthKeys, <>drumKeys, <>root, repeat;
	*new {
		^super.new.init_quickkeyboard
	}
	init_quickkeyboard {
		repeat = Array.new;
		synthKeys = Dictionary[
			$a -> 0,
			$w -> 1,
			$s -> 2,
			$e -> 3,
			$d -> 4,
			$f -> 5,
			$t -> 6,
			$g -> 7,
			$y -> 8,
			$h -> 9,
			$u -> 10,
			$j -> 11,
			$k -> 12,
			$o -> 13,
			$l -> 14,
			$p -> 15,
			$; -> 16,
			$' -> 17
		];
		drumKeys = Dictionary[
			$z -> 50,
			$x -> 45,
			$c -> 51,
			$v -> 49,
			$b -> 36,
			$n -> 38,
			$m -> 46,
			$, -> 42
		];
		root = 52;
		this.setGlobalKeyActions;
	}
	setGlobalKeyActions {
		GUI.view.globalKeyDownAction = { |src,char,mod,unic,keyc|
			if(repeat.includes(char).not){
				repeat = repeat.add(char);
				if(synthKeys[char].notNil){
					TheInstrument.noteOn(nil, 0, synthKeys[char] + root, 90);
				};
				if(drumKeys[char].notNil){
					TheInstrument.noteOn(nil, 9, drumKeys[char], 90);
				};
				keyc.switch(
					123, {
						root = root - 12;
					},
					124, {
						root = root + 12;
					}
				);
			};
		};
		GUI.view.globalKeyUpAction = { |obj,char,mod,unic,keyc|
			if(repeat.includes(char)){
				repeat.remove(char);
			};
			if(synthKeys[char].notNil){
				TheInstrument.noteOff(nil,0, synthKeys[char] + root,0);
			};
		};
	}
}

GenericActionButton {
	var <>mouseDownAction, <>mouseUpAction;
	*new {
		^super.new.init_genericactionbutton;
	}
	init_genericactionbutton {
		mouseDownAction = { |val| val.postln; };
		mouseUpAction = { |val| val.postln; };
		this.makeGUI;
	}
	makeGUI {
		var win, but;
		win = GUI.window.new("act", Rect.new(500.rand, 500.rand, 300, 200)).front;
		but = GUI.button.new(win, Rect.new(0, 0, 290, 190))
			.states_([["test", Color.green, Color.black]])
			.mouseDownAction_({ |obj| mouseDownAction.value(obj.value); })
			.mouseUpAction_({ |obj| mouseUpAction.value(obj.value); });
	}
}