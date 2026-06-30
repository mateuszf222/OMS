#!/usr/bin/env node
// EVT-VER-1: wykrywa zmiany ŁAMIĄCE w zdarzeniach AsyncAPI 3.0 między dwiema wersjami pliku.
//
// Dlaczego nie samo @asyncapi/diff: dla AsyncAPI 3.0 wykrywa ono zmiany, ale NIE klasyfikuje
// ich (wszystko trafia do 'unclassified' — reguły klasyfikacji są pisane pod 2.x). Dlatego
// używamy @asyncapi/diff jako silnika wykrywania zmian, a klasyfikację breaking robimy sami,
// po ścieżce/akcji zmiany. $ref (payloady -> data-contracts) rozwiązujemy json-schema-ref-parser.
//
// Użycie: node scripts/asyncapi-breaking.mjs <base.yaml> <revision.yaml>
import process from 'node:process';
import * as diffMod from '@asyncapi/diff';
import $RefParser from '@apidevtools/json-schema-ref-parser';

const diff = [diffMod.diff, diffMod.default, diffMod].find((c) => typeof c === 'function');

const [baseFile, revFile] = process.argv.slice(2);
if (!baseFile || !revFile) {
  console.error('uzycie: node scripts/asyncapi-breaking.mjs <base> <revision>');
  process.exit(2);
}

// Klasyfikacja 3.0-aware: co uznajemy za zmianę łamiącą konsumenta.
function breakingReason(change) {
  const p = change.path || '';
  const a = change.action;
  if (a === 'remove' && /^\/channels\/[^/]+$/.test(p)) return 'usunięty kanał (topic)';
  if (a === 'remove' && /\/messages\/[^/]+$/.test(p)) return 'usunięty message z kanału';
  if (a === 'edit' && /\/channels\/[^/]+\/address$/.test(p)) return 'zmiana adresu topicu';
  if (a === 'remove' && /\/payload\/properties\/[^/]+$/.test(p)) return 'usunięte pole payloadu';
  if (a === 'edit' && /\/type$/.test(p)) return 'zmiana typu pola payloadu';
  if (a === 'add' && /\/required\/\d+$/.test(p)) return 'nowe pole wymagane (required)';
  return null; // dodanie pola/kanału/message, zmiana opisu itp. = nie-łamiące
}

const base = await $RefParser.dereference(baseFile);
const rev = await $RefParser.dereference(revFile);

const result = diff(base, rev);
const allChanges = [...result.breaking(), ...result.unclassified()];
const seen = new Set();
const breaking = allChanges
  .map((c) => ({ action: c.action, path: c.path, why: breakingReason(c) }))
  .filter((c) => c.why)
  .filter((c) => {
    // Dedup: po dereferencji kanał jest inline'owany także w /operations/<op>/channel,
    // więc ta sama zmiana bywa raportowana dwukrotnie. Normalizujemy prefiks.
    const norm = c.path.replace(/^\/operations\/[^/]+\/channel/, '').replace(/^\/channels\/[^/]+/, '');
    const key = `${c.why}|${c.action}|${norm}`;
    if (seen.has(key)) return false;
    seen.add(key);
    return true;
  });

if (breaking.length === 0) {
  console.log(`OK: brak zmian łamiących (${baseFile} -> ${revFile}).`);
  process.exit(0);
}

console.error(`ZMIANY ŁAMIĄCE (${breaking.length}) w ${revFile} — wymagany nowy topic .vN+1 (EVT-VER-1):`);
for (const c of breaking) console.error(`  - [${c.why}] ${c.action} ${c.path}`);
process.exit(1);
