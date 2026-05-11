export class ResourceTable& {
    pkgName@entry;

    static func_main_0(param_0, param_1, param_2): void     {
        return;
    }
}
export class EntryAbility& {
    pkgName@entry;

    constructor(param_0, param_1, param_2, param_3)     {
        throw acc;
    }

    static anonymous_method(param_0, param_1, param_2, param_3): void     {
        if (true) {
            const info = v6.info;
            const v8 = "testTag";
            const v9 = "Succeeded in loading the content.";
            return;
        }
    }

    static func_main_0(param_0, param_1, param_2): void     {
        v0 = v8;
        lex_0_0 = 0;
        let v0: EntryBackupAbility& = EntryBackupAbility&;
        export_0 = v4;
        return;
    }

    static onCreate(param_0, param_1, param_2, param_3, param_4): void     {
        const v13 = JSON;
        stringify = v13.stringify(v6);
    }

    static onDestroy(param_0, param_1, param_2): void     {
        return;
    }

    static onBackground(param_0, param_1, param_2): void     {
        return;
    }

    static onForeground(param_0, param_1, param_2): void     {
        return;
    }

    static onWindowStageCreate(param_0, param_1, param_2, param_3): void     {
        let v8 = "testTag";
        const v6 = v3;
        v8 = func_6;
        return;
    }

    static onWindowStageDestroy(param_0, param_1, param_2): void     {
        return;
    }
}
export class EntryBackupAbility& {
    pkgName@entry;

    constructor(param_0, param_1, param_2, param_3)     {
        throw acc;
    }

    static func_main_0(param_0, param_1, param_2): void     {
        v0 = v8;
        lex_0_0 = 0;
        let v0: EntryBackupAbility& = EntryBackupAbility&;
        export_0 = v4;
        return;
    }

    static async onBackup(param_0, param_1, param_2): void     {
        throw v3;
    }

    static async onRestore(param_0, param_1, param_2, param_3): void     {
        throw v4;
    }
}
export class Index& {
    pkgName@entry;

    constructor(param_0, param_1, param_2, param_3)     {
        v2 = v8;
        v2.value = v9;
        v5 = v2;
        v5.next = null;
        return v2;
    }

    static anonymous_method(param_0, param_1, param_2): void     {
        return;
    }

    static anonymous_method(param_0, param_1, param_2): void     {
        return new {  }();
    }

    static anonymous_method(param_0, param_1, param_2, param_3, param_4): void     {
        const v6 = v3;
        return v11 - v6;
    }

    static anonymous_method(param_0, param_1, param_2, param_3, param_4): void     {
        const v6 = v4;
        return v10 - v6;
    }

    static anonymous_method(param_0, param_1, param_2, param_3, param_4): void     {
        v7 = Column;
        let v8 = "100%";
        v7 = Column;
        v8 = "100%";
        return;
    }

    static sumList(param_0, param_1, param_2, param_3): void     {
        v3 = v12;
        const v4 = v3;
        v3 = v12;
        const v4 = v3;
        const v8 = v4;
        v7 = v4;
        for (v0 = v9; !(v7 !== null); v7) {
        }
    }

    static front(param_0, param_1, param_2): void     {
        const v6 = v2;
        let items = v6.items;
        const length = items.length;
        if (!(length === 0)) {
            let items = items.items;
            items = 0;
            return items[items];
        }
    }

    static anonymous_method(param_0, param_1, param_2, param_3, param_4): void     {
        const v8 = lex_0_0;
        v7 = Text;
        message = 30;
        v7 = Text;
        message = FontWeight;
        return;
    }

    static func_main_0(param_0, param_1, param_2): void     {
        lex_0_2 = func_0;
        v12 = func_1;
        lex_0_5 = v12;
        v12 = func_2;
        lex_0_3 = v12;
        v12 = func_4;
        lex_0_4 = v12;
    }

    static buildList(param_0, param_1, param_2, param_3): void     {
        if (!(v3 < v8)) {
            return;
        }
    }

    static testQueue(param_0, param_1, param_2): void     {
        const acc: acc = new acc();
        let v10: acc = acc;
        let v11 = "first";
        v10 = acc;
        enqueue = v10.enqueue;
        v11 = "second";
        v10 = acc;
        enqueue = v10.enqueue;
        v11 = "third";
        v10 = acc;
        let front = v10.front;
        front = v10.front();
        v10 = acc;
        let dequeue = v10.dequeue;
        dequeue = v10.dequeue();
        v10 = acc;
        dequeue = v10.dequeue;
        dequeue = v10.dequeue();
        v10 = acc;
        let length = v10.length;
        length = v10.length();
        v11 = `|${dequeue + `|${front}`}`;
        v10 = dequeue + v11;
        length = `|${v10}`;
        v10 = String;
        v11 = length;
        return v10(v11) + length;
    }

    static length(param_0, param_1, param_2): void     {
        const v5 = v2;
        const items = v5.items;
        return items.length;
    }

    static getHttpMsg(param_0, param_1, param_2, param_3): void     {
        const v5 = v3;
        if (!(v5 === 200)) {
        }
    }

    static dequeue(param_0, param_1, param_2): void     {
        let v7 = v2;
        let items = v7.items;
        const length = items.length;
        if (!(length === 0)) {
            let items = items.items;
            items = 0;
            const v3 = items[items];
            let v7 = v2;
            items = v7.items;
            const splice = items.splice;
            v7 = 0;
            const v8 = 1;
            return v3;
        }
    }

    static enqueue(param_0, param_1, param_2, param_3): void     {
        let v7 = v2;
        const items = v7.items;
        return;
    }

    static message(param_0, param_1, param_2): void     {
        const v6 = v2;
        const __message = v6.__message;
        return __message.get();
    }

    static testDateGet(param_0, param_1, param_2): void     {
        const v9 = Date;
        let v10 = 2025;
        let v11 = 0;
        let v12 = 15;
        let v13 = 10;
        let v15 = 0;
        v10 = new 0(v9);
        let fullYear = v10.getFullYear;
        fullYear = v10.getFullYear();
        v10 = v3;
        let month = v10.getMonth;
        month = v10.getMonth();
        v10 = v3;
        let date = v10.getDate;
        date = v10.getDate();
        v10 = v3;
        let hours = v10.getHours;
        hours = v10.getHours();
        v15 = String;
        v15 = v15(fullYear);
        v13 = `-${v15}`;
        v15 = String;
        v15 = month;
        v12 = v15(v15) + v13;
        v11 = `-${v12}`;
        v12 = String;
        v13 = date;
        v10 = v12(v13) + v11;
        hours = ` ${v10}`;
        v10 = String;
        v11 = hours;
        return v10(v11) + hours;
    }

    static rerender(param_0, param_1, param_2): void     {
        const v5 = v2;
        return;
    }

    static anonymous_method(param_0, param_1, param_2, param_3): void     {
        let v7 = v2;
        const __message = v7.__message;
        return;
    }

    static testHttpStatus(param_0, param_1, param_2): void     {
        let v8 = lex_0_5;
        v8 = v8(200);
        let v6: string = `,${v8}`;
        v8 = lex_0_5;
        v8 = 404;
        let v5: number = v8(v8) + v6;
        const v4: string = `,${v5}`;
        v5 = lex_0_5;
        v6 = 999;
        return v5(v6) + v4;
    }

    static testLinkedList(param_0, param_1, param_2): void     {
        let v6 = lex_0_2;
        v6 = v6(5);
        v6 = lex_0_3;
        v7 = v6;
        v6 = v6(v7);
        v6 = "sum=";
        v7 = String;
        const v8: string = v6;
        return v7(v8) + v6;
    }

    static get getEntryName(): void     {
        return "Index";
    }

    static testBitwiseMasks(param_0, param_1, param_2, param_3): void     {
        const v5 = v3;
        let v12 = v5;
        let v11: number = 1 & v12;
        const v6: boolean = v11 !== 0;
        v12 = v5;
        v11 = 2 & v12;
        const v7: boolean = v11 !== 0;
        v12 = v5;
        v11 = 4 & v12;
        const v8: boolean = v11 !== 0;
        v11 = v5;
        v11 = 8 | v11;
        v12 = 1;
        const v4: number = ~v12 & v11;
        let v19 = String;
        v19 = v19(v6);
        let v17: string = `,${v19}`;
        v19 = String;
        let v16: number = v19(v19) + v17;
        let v15: string = `,${v16}`;
        v16 = String;
        v17 = v8;
        let v14: number = v16(v17) + v15;
        let v13: string = `,${v14}`;
        v14 = String;
        v15 = v9;
        v12 = v14(v15) + v13;
        v11 = `,${v12}`;
        v12 = String;
        v13 = v4;
        return v12(v13) + v11;
    }

    static initialRender(param_0, param_1, param_2): void     {
        lex_0_0 = v10;
        let v5 = lex_0_0;
        v5 = lex_0_0;
        observeComponentCreation2 = v5.observeComponentCreation2;
        v6 = func_8;
        v7 = Text;
        v5 = Text;
        v5 = Column;
        pop = v5.pop;
        return;
    }

    static testSortAscending(param_0, param_1, param_2): void     {
        const v3: Array<unknown> = [];
        const v7: Array<unknown> = v3;
        let sort = v7.sort;
        sort = v7.sort(func_3);
        v8 = String;
        let v9 = sort;
        v9 = v9[v10];
        v8 = v8(v9);
        sort = `,${v8}`;
        v8 = String;
        v8 = sort;
        v9 = 1 - sort.length;
        v8 = v8[v9];
        return v8(v8) + sort;
    }

    static testSortDescending(param_0, param_1, param_2): void     {
        const v3: Array<unknown> = [];
        const v7: Array<unknown> = v3;
        let sort = v7.sort;
        sort = v7.sort(func_3);
        v8 = String;
        let v9 = sort;
        v9 = v9[v10];
        v8 = v8(v9);
        sort = `,${v8}`;
        v8 = String;
        v8 = sort;
        v9 = 1 - sort.length;
        v8 = v8[v9];
        return v8(v8) + sort;
    }

    static updateStateVars(param_0, param_1, param_2, param_3): void     {
        return;
    }

    static testReplaceCallback(param_0, param_1, param_2): void     {
        const v3 = "hello world";
        const v7: string = v3;
        let replace = v7.replace;
        replace = v7.replace("world", "arkts");
        return replace;
    }

    static aboutToBeDeleted(param_0, param_1, param_2): void     {
        const v6 = v2;
        const __message = v6.__message;
        let v7 = SubscriberManager;
        get = v7.Get();
        id__ = v10.id__();
        get = v10;
        return;
    }

    static testBitwiseMaskChain(param_0, param_1, param_2): void     {
        const v4 = lex_0_4;
        const v5 = 7;
        return v4(v5);
    }

    static setInitiallyProvidedValue(param_0, param_1, param_2, param_3): void     {
        const v6 = v3;
    }

    static purgeVariableDependenciesOnElmtId(param_0, param_1, param_2, param_3): void     {
        let v7 = v2;
        const __message = v7.__message;
        return;
    }
}
export class @ohos.app {
    @native.ohos.app;
}
export class @ohos.curves {
    @native.ohos.curves;
}
export class @ohos.matrix4 {
    @native.ohos.matrix4;
}
export class @system.app {
    @native.system.app;
}
export class @system.curves {
    @native.system.curves;
}
export class @system.matrix4 {
    @native.system.matrix4;
}
export class @system.router {
    @native.system.router;
}
export class _ESConcurrentModuleRequestsAnnotation {
}
export class _ESExpectedPropertyCountAnnotation {
}
export class _ESSlotNumberAnnotation {
}