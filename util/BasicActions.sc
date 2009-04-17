QuickKeyboard {
	var synthKeys, drumKeys, <>root;
	*new {
		^super.new.init_quickkeyboard
	}
	init_quickkeyboard {
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
		GUI.view.globalKeyDownAction = { |src,char,mod,unic,keyc|
			synthKeys[char].notNil.if{
				TheInstrument.noteOnFunction.(nil, 0, synthKeys[char] + root, 90);
			};
			drumKeys[char].notNil.if{
				TheInstrument.noteOnFunction.(nil, 9, drumKeys[char], 90);
			}
		};
		GUI.view.globalKeyUpAction = { |obj,char,mod,unic,keyc|
			synthKeys[char].notNil.if{
				TheInstrument.noteOffFunction.(nil,0, synthKeys[char] + root,0);
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