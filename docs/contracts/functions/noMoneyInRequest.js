'use strict';

// API-SEC-1: w ciele żądania nie wolno przenosić kwot/cen. Skanuje (po rozwiązaniu $ref)
// schemat requestu i zgłasza każde pole o nazwie sugerującej kwotę.
var FORBIDDEN = ['amount', 'price', 'total', 'unitprice', 'cost', 'subtotal', 'grandtotal', 'money'];

module.exports = function noMoneyInRequest(schema, _opts, context) {
  var results = [];
  var basePath = (context && context.path) || [];

  function scan(node, trail) {
    if (!node || typeof node !== 'object') return;
    if (node.properties && typeof node.properties === 'object') {
      Object.keys(node.properties).forEach(function (key) {
        if (FORBIDDEN.indexOf(String(key).toLowerCase()) !== -1) {
          results.push({
            message: "Pole '" + key + "' wyglada na kwote — zakaz kwot w zadaniu (API-SEC-1).",
            path: basePath.concat(trail, ['properties', key])
          });
        }
        scan(node.properties[key], trail.concat(['properties', key]));
      });
    }
    if (node.items) scan(node.items, trail.concat(['items']));
    ['allOf', 'anyOf', 'oneOf'].forEach(function (comb) {
      if (Array.isArray(node[comb])) {
        node[comb].forEach(function (s, i) { scan(s, trail.concat([comb, String(i)])); });
      }
    });
  }

  scan(schema, []);
  return results;
};
