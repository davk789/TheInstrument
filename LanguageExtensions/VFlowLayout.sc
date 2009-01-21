
VFlowLayout {
	var <bounds, <>margin, <>gap;
	var <>left, <>top, <>maxHeight,<>maxRight, <>maxWidth, <>maxBottom;

	*new { arg bounds, margin, gap;
		^super.newCopyArgs(bounds, margin, gap).init
	}
	init {
		gap = gap ? Point(4,4);
		margin = margin ? Point(4,4);
		this.reset;
	}
	reset {
		maxRight = left = bounds.left + margin.x;
		maxBottom = top = bounds.top + margin.y;
		top = bounds.top + margin.y;
		left = bounds.left + margin.x;
		maxHeight  = 0;
		maxWidth = 0;
	}
	place { arg view;
		var height, width,vbounds;
		vbounds = view.bounds;
		width = vbounds.width;
		height = vbounds.height;
		if ((top + height) > (bounds.bottom - margin.y), { this.nextColumn; });

		view.bounds = Rect(left, top, width, height);

		maxBottom = max(maxBottom, top + height);
		top = top + height + gap.y;
		maxWidth = max(maxWidth, width);
	}
	nextColumn {
		
		top = bounds.top + margin.y;
		left = left + maxWidth + gap.x;
		maxHeight = 0;
	}
	shift { arg x=0, y=0;
		left = left + x;
		top = top + y;
	}
	innerBounds {
		^bounds.insetBy(margin.x * 2,margin.y * 2)
	}
	bounds_ { arg b;
		var d;
		top = top + ( d = (b.top - bounds.top));
		maxBottom = maxBottom + (d);
		left = left + (d = (b.left - bounds.left));
		maxWidth = maxWidth + (d);
		bounds = b;
		// and then you need to re-place all views
		// but nextLine will be broken, see FlowView
	}
	currentBounds {
		var currentBounds;
		currentBounds = bounds;
		currentBounds.width = left + maxWidth;
		^currentBounds
	}
	// rounded out to the nearest rect + margin
	used {
		^Rect(bounds.left,bounds.top,
			maxRight + margin.x - bounds.left,
			(top + maxHeight + margin.y ) - bounds.top)
	}
	// largest allocatable rect starting in the current row
	// going down as far as possible
	indentedRemaining {
		var inb;
		inb = this.innerBounds;
		^Rect(left,top,
			inb.width - (left - inb.left - margin.x),
			inb.height - (top - inb.top - margin.y))
	}
}


