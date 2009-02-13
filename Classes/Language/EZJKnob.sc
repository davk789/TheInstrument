EZJKnob { // uses JKnob. this is might become obsolete.
    var <knobView, <label, <knob, <numberBox, labelText, >knobAction, controlSpec,
		prValue=0;
    *new { |parent, dimensions, lab|
        ^super.new.init_ezknobDK(parent, dimensions, lab);
    }
    init_ezknobDK { |parent, dimensions, lab=("Knob")|
        labelText = lab;
        knobAction = { |val| val.postln; };
        knobView = GUI.vLayoutView.new(parent, dimensions).background_(Color.blue(0.2, alpha:0.1));
        this.addControls;
        numberBox.value = knob.value;
    }
    addControls {
        label = GUI.staticText.new(knobView, Rect.new(0, 0, 0, knobView.bounds.height / 4.2))
            .string_(labelText)
			.align_('center');
        knob = JKnob.new(knobView, Rect.new(0, 0, 0, knobView.bounds.height / 2.1))
            .action_({|obj|
				if(controlSpec.isKindOf(ControlSpec)){
					prValue = controlSpec.map(obj.value);
				}{
					prValue = obj.value;
				};
                knobAction.value(prValue);
                numberBox.value = prValue;
            });
        numberBox = GUI.numberBox.new(knobView, Rect.new(0, 0, 0, knobView.bounds.height / 4.2));
    }
    knobValueAction {
    	knobAction.value;
    }
    zeroOneValue_ { |val|
		this.value_(controlSpec.map(val));
    }
	value_ { |val|
		prValue = val;
		if(controlSpec.isKindOf(ControlSpec)){
			knob.value = controlSpec.unmap(prValue);
		}{
			knob.value = prValue;
		};
		numberBox.value = prValue;
	}
	knobColor_ { |colors|
		knob.color = colors;
	}
	spec_ { |sp|
		controlSpec = sp;
		this.value = prValue;
	}

}
                       