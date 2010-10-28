ListMaker {
	var numFields, numEntries=1;
	*new { |fields, list|
		^super.new.init_listmaker(fields, list);
	}
	init_listmaker { |fields=2,list|
		numFields = fields;
		this.makeGUI;
	}
	addEntry { |val,row,col|
		
	}
	makeGUI {
		var win, row;
		win = GUI.window.new("edit list", Rect.new(500.rand, 500.rand, 310, 200))
			.front;
		win.view.decorator_(FlowLayout(win.view.bounds))
			.background_(Color.black);
		numEntries.do{ |rowNum|
			row = GUI.hLayoutView.new(win, Rect.new(0, 0, 300, 45))
				.background_(Color.white.alpha_(0.9));
			numFields.do{ |colNum|
				GUI.numberBox.new(row, Rect.new(0, 0, 50, 0))
					.action_(this.addEntry(this.value, rowNum, colNum));
			}
		};
	}

}