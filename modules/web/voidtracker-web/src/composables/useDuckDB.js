import * as duckdb from '@duckdb/duckdb-wasm';
import { ref } from 'vue';

const db = ref(null);
const conn = ref(null);
const isReady = ref(false);

export function useDuckDB() {

    async function init() {
        if (isReady.value) return;

        const JSDELIVR_BUNDLES = duckdb.getJsDelivrBundles();
        const bundle = await duckdb.selectBundle(JSDELIVR_BUNDLES);

        const worker_url = URL.createObjectURL(
            new Blob([`importScripts("${bundle.mainWorker}");`], { type: 'text/javascript' })
        );

        const worker = new Worker(worker_url);
        const logger = new duckdb.ConsoleLogger();

        const database = new duckdb.AsyncDuckDB(logger, worker);
        await database.instantiate(bundle.mainModule, bundle.pthreadWorker);

        db.value = database;
        conn.value = await database.connect();
        isReady.value = true;
        console.log("🦆 DuckDB-Wasm Initialized!");
    }

    async function query(sql) {
        if (!conn.value) await init();
        return await conn.value.query(sql);
    }

    async function importJSON(tableName, jsonArray) {
        if (!db.value) await init();
        await db.value.registerFileText(`${tableName}.json`, JSON.stringify(jsonArray));
        await conn.value.insertJSONFromPath(`${tableName}.json`, { name: tableName });
    }

    return {
        init,
        query,
        importJSON,
        isReady
    };
}
