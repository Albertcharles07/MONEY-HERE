const {test}=require('node:test');const assert=require('node:assert/strict');const {compound,emergency,debt,retirement,credit}=require('../lib/calculators.cjs');
test('juros compostos simples',()=>assert.ok(Math.abs(compound(100,0,2,.01).total-102.01)<.00001));
test('aportes sem rendimento',()=>assert.equal(compound(0,100,12,0).total,1200));
test('reserva',()=>assert.deepEqual(emergency(2000,6,1000),{target:12000,remaining:11000}));
test('divida sem juros',()=>assert.equal(debt(1200,0,100).periods,12));
test('parcela insuficiente',()=>assert.throws(()=>debt(1000,120,1)));
test('aposentadoria',()=>assert.equal(retirement(1200,0,1,0),100));
test('credito',()=>assert.equal(credit(1000,12,110).extra,320));
