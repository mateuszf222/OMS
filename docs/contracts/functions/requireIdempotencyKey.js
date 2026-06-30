'use strict';

// API-IDEM-1: POST tworzący zasób / o nieidempotentnym skutku musi przyjmować nagłówek
// Idempotency-Key. Wyjątek: operacje oznaczone tagiem "webhook" (kontrakt zewnętrzny).
module.exports = function requireIdempotencyKey(operation, _opts, context) {
  if (!operation || typeof operation !== 'object') return [];

  var tags = Array.isArray(operation.tags) ? operation.tags : [];
  if (tags.indexOf('webhook') !== -1) return [];

  var params = Array.isArray(operation.parameters) ? operation.parameters : [];
  var has = params.some(function (p) {
    return p && p.name === 'Idempotency-Key' && p.in === 'header';
  });
  if (has) return [];

  return [{
    message: 'POST wymaga naglowka Idempotency-Key (API-IDEM-1).',
    path: (context && context.path) || []
  }];
};
