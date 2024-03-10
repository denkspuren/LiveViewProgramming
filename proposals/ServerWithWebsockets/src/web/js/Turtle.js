window.Turtle = window.Turtle || (function () {

    const size = {
        width: 800,
        height: 800
    };

    // CC Attribution License by Aslan
    const svg = [
        {
            path: new Path2D('M851.2 255.7c15.9 16 24.7 37.1 24.7 59.7s-8.8 43.7-24.7 59.7L790.3 436c5 21.1 7.6 43.1 7.6 65.6v89.9c0 22.3-2.6 44-7.4 64.8-4.8 20.8-12 40.8-21.1 59.6l32.7 32.7c38.3 38.3 38.3 100.6 0 138.9-18.6 18.6-43.2 28.8-69.4 28.8-26.2 0-50.9-10.2-69.4-28.8l-34.8-34.8c-15.5 6.9-31.7 12.5-48.5 16.6l-16.7 42.8c-16 40.9-39 47-51.2 47-12.3 0-35.2-6.1-51.2-47L444 869.4c-16.8-4.1-33-9.7-48.5-16.6l-34.8 34.8c-18.6 18.6-43.2 28.8-69.4 28.8s-50.9-10.2-69.4-28.8c-18.6-18.5-28.8-43.2-28.8-69.4 0-26.2 10.2-50.9 28.8-69.4l32.7-32.7c-18.3-37.7-28.5-79.9-28.5-124.5v-89.9c0-22.6 2.6-44.5 7.6-65.6l-60.9-60.9c-15.9-16-24.7-37.1-24.7-59.7 0-22.5 8.8-43.7 24.7-59.7 15.9-15.9 37.1-24.7 59.7-24.7s43.7 8.8 59.7 24.7l31.2 31.2c12.9-11.4 26.9-21.6 41.7-30.5 14.8-8.9 30.5-16.5 46.9-22.7V165c0-55.2 44.9-100.1 100.1-100.1 55 0 99.9 44.9 99.9 100.1v68.7c32.8 12.3 62.8 30.5 88.6 53.2l31.2-31.2c15.9-15.9 37.1-24.7 59.7-24.7 22.5 0 43.7 8.8 59.7 24.7z'),
            color: '#663333'
        },
        {
            path: new Path2D('M822.5 284.4c8.3 8.3 12.9 19.3 12.9 31s-4.6 22.8-12.9 31l-46.2 46.2c-5.8-14.1-12.8-27.6-20.7-40.5-7.9-12.8-16.8-25-26.6-36.4l31.3-31.3c8.3-8.3 19.3-12.9 31-12.9 11.9 0 22.9 4.6 31.2 12.9zM773.4 777.4c22.5 22.5 22.5 59.1 0 81.6-10.9 10.9-25.4 16.9-40.8 16.9-15.4 0-29.9-6-40.8-16.9l-26.3-26.3c16.3-10.5 31.6-22.5 45.5-36 13.9-13.5 26.4-28.3 37.3-44.4l25.1 25.1z'),
            color: '#F9DCB2'
        },
        {
            path: new Path2D('M757.4 566.8v24.7c0 75.3-34.1 142.8-87.7 187.9l-64.5-111.7 58.3-100.9h93.9z'),
            color: '#9ACE57'
        },
        {
            path: new Path2D('M757.4 501.6v24.7h-94l-56.3-97.6 65.1-112.8c52.2 45 85.2 111.6 85.2 185.7z'),
            color: '#6A973A'
        },
        {
            path: new Path2D('M639.3 291.8l-65.4 113.4H453.7l-66.4-115c18.3-10.8 38.1-19.4 59-25.2 20.9-5.8 42.9-8.9 65.7-8.9 46.6 0 90.2 13.1 127.3 35.7z'),
            color: '#9ACE57'
        },
        {
            path: new Path2D('M570.2 687.9l66.4 115c-3.3 1.9-6.6 3.8-10 5.6h-0.1c-2.1 1.2-4.3 2.2-6.5 3.3C587.4 828 550.7 837 511.9 837c-38.7 0-75.4-9-108.1-25.1-2.2-1.1-4.4-2.1-6.5-3.3-2.4-1.3-4.9-2.6-7.3-4l67.3-116.7h112.9zM570.2 445.7l58.2 100.9-58.2 100.8H453.7l-58.2-100.8 58.2-100.9z'),
            color: '#6A973A'
        },
        {
            path: new Path2D('M571.5 177.7v44.2c-9.6-2-19.4-3.6-29.3-4.7-9.9-1-20-1.6-30.2-1.6-20.4 0-40.4 2.1-59.6 6.3v-44.2c3.3 2.6 7.4 4.2 11.9 4.2 10.5 0 19-8.5 19-19s-8.5-19-19-19c-3.2 0-6.2 0.8-8.9 2.2 7.9-23.6 30.2-40.6 56.5-40.6 26.2 0 48.5 17.1 56.5 40.6-2.6-1.4-5.6-2.2-8.8-2.2-10.5 0-19 8.5-19 19s8.5 19 19 19c4.5 0 8.6-1.6 11.9-4.2zM533.5 876.7l-8.1 20.7c-5.7 14.7-11.4 20-13.5 21.1-2.1-1.1-7.8-6.5-13.5-21.1l-8.1-20.7c7.1 0.5 14.3 0.8 21.6 0.8 7.3 0 14.5-0.2 21.6-0.8z'),
            color: '#F9DCB2'
        },
        {
            path: new Path2D('M360.4 566.8l60.1 104.1-63.9 110.6c-27.5-22.5-50-50.9-65.7-83.2-15.7-32.3-24.4-68.5-24.4-106.8v-24.7h93.9z'),
            color: '#9ACE57'
        },
        {
            path: new Path2D('M354.1 313.7l64.5 111.7-58.2 100.8h-94v-24.7c0.1-75.2 34.1-142.7 87.7-187.8z'),
            color: '#6A973A'
        },
        {
            path: new Path2D('M358.3 832.7L332 858.9c-10.9 10.9-25.4 16.9-40.8 16.9-15.4 0-29.9-6-40.8-16.9-10.9-10.9-16.9-25.4-16.9-40.8 0-15.4 6-29.9 16.9-40.8l25.1-25.1c10.9 16 23.5 30.9 37.3 44.4 13.9 13.5 29.1 25.6 45.5 36.1zM263.4 284.4l31.3 31.3c-19.6 22.8-35.6 48.8-47.2 76.9l-46.2-46.2c-8.3-8.3-12.9-19.3-12.9-31s4.6-22.7 12.9-31 19.3-12.9 31-12.9 22.8 4.6 31.1 12.9z'),
            color: '#F9DCB2'
        }
    ];

    Turtle.prototype = Object.create(Object.prototype);
    Turtle.prototype.constructor = Turtle;

    Turtle.prototype.update = function(data) {
        if (!data.position) {
            this.getHistory().push({
                x: this.position.x,
                y: this.position.y
            });
        }

        for (let key in data) {
            if (this.hasOwnProperty(key)) {
                this[key] = data[key];
            }
        }
    };

    function Turtle(id) {
        Object.call(this);

        let moving = false;
        const awaiting = [];
        const history = [];

        this.getHistory = function() {
            return history;
        };

        this.await = function(callback) {
            if (!moving) {
                callback(this);
            } else {
                awaiting.push(callback);
            }
        };

        this.id = id;

        this.position = {
            x: 0,
            y: 0
        };

        this.destination = {
            x: 0,
            y: 0
        };

        this.scale = {
            x: 0.1,
            y: 0.1
        };

        this.direction = {
            x: 0,
            y: -1
        };

        this.speed = 3;

        this.render = function(context) {
            if (Math.ceil(this.destination.x) !== Math.ceil(this.position.x) || Math.ceil(this.destination.y) !== Math.ceil(this.position.y)) {
                if (!moving) {
                    history.push({ x: this.position.x, y: this.position.y });
                    moving = true;
                }

                const distance = {
                    x: this.destination.x - this.position.x,
                    y: this.destination.y - this.position.y
                };
                const length = Math.sqrt(distance.x * distance.x + distance.y * distance.y);

                this.direction.x = distance.x / length;
                this.direction.y = distance.y / length;

                const delta = {
                    x: this.speed * this.direction.x,
                    y: this.speed * this.direction.y
                };

                this.position.x += delta.x < 0 ? Math.max(delta.x, distance.x) : Math.min(delta.x, distance.x);
                this.position.y += delta.y < 0 ? Math.max(delta.y, distance.y) : Math.min(delta.y, distance.y);
            } else if (moving) {
                while (awaiting.length > 0) {
                    const callback = awaiting.pop();
                    callback(this);
                }
                moving = false;
            }

            context.strokeStyle = '#992222';
            context.lineWidth = 2;
            context.beginPath();
            if (history.length > 0) {
                context.moveTo(history[0].x, history[0].y);
            }
            for (let i = 1; i < history.length; ++i) {
                context.lineTo(history[i].x, history[i].y);
            }
            context.lineTo(this.position.x, this.position.y);
            context.stroke();

            context.translate(this.position.x, this.position.y);
            context.rotate(Math.atan2(this.direction.y, this.direction.x) + Math.PI / 2); // be careful, for some reason the API swapped x and y here ...
            context.scale(this.scale.x, this.scale.y);
            context.translate(-(size.width / 2) - 110, -(size.height / 2));

            for (let i = 0; i < svg.length; ++i) {
                context.fillStyle = svg[i].color;
                context.beginPath();
                context.fill(svg[i].path);
            }
        };

    }

    return Turtle;
})();
