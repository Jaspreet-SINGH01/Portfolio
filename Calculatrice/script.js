let display = document.getElementById("display");
let buttons = document.getElementsByTagName("button");
let clear = document.getElementById("clear");
let equal = document.getElementById("equal");
let operator = document.getElementsByClassName("operator");

for (let i = 0; i < buttons.length; i++) {
    buttons[i].addEventListener("click", function() {
        let value = this.getAttribute("value");
        display.value += value;
    });
}

for (let i = 0; i < operator.length; i++) {
    operator[i].addEventListener("click", function() {
        operator = this.getAttribute("value");
    });
}

clear.addEventListener("click", function() {
    display.value = "";
});

equal.addEventListener("click", function() {
    display.value = eval(display.value);
});
