class Dot {
    constructor(container) {
        this.container = container;
    }
    draw(dotString) {
        const container = this.container;
        Viz.instance().then(function(viz) {
            container.appendChild(viz.renderSVGElement(dotString));
        });
    }
}