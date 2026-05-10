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
        v2.name = v9;
        return v2;
    }

    static anonymous_method(param_0, param_1, param_2): void     {
        return;
    }

    static anonymous_method(param_0, param_1, param_2): void     {
        return new {  }();
    }

    static anonymous_method(param_0, param_1, param_2, param_3): void     {
        const v5 = v3;
        return v5 < 25;
    }

    static anonymous_method(param_0, param_1, param_2, param_3): void     {
        const v5 = v3;
        return v5 < 25;
    }

    static anonymous_method(param_0, param_1, param_2, param_3, param_4): void     {
        v7 = Column;
        let v8 = "100%";
        v7 = Column;
        v8 = "100%";
        return;
    }

    static get reset(): void     {
        v4.tally = 0;
        return;
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
        throw acc;
    }

    static getName(param_0, param_1, param_2): void     {
        const v4 = v2;
        return v4.name;
    }

    static message(param_0, param_1, param_2): void     {
        const v6 = v2;
        const __message = v6.__message;
        return __message.get();
    }

    static getBreed(param_0, param_1, param_2): void     {
        const v4 = v2;
        return v4.breed;
    }

    static get getCount(): void     {
        return v4.tally;
    }

    static rerender(param_0, param_1, param_2): void     {
        const v5 = v2;
        return;
    }

    static get increment(): void     {
        v4.tally = 1 + v6.tally;
        return v4.tally;
    }

    static anonymous_method(param_0, param_1, param_2, param_3): void     {
        let v7 = v2;
        const __message = v7.__message;
        return;
    }

    static testInstanceof(param_0, param_1, param_2, param_3): void     {
        if (true) {
            const name = v6.getName;
            return v6.getName();
        }
    }

    static testObjectKeys(param_0, param_1, param_2): void     {
        {  }[1] = "x";
        v6 = v4;
        v7 = "y";
        v6[2] = v7;
        v6 = v4;
        v7 = "z";
        v6[3] = v7;
        v7 = Object;
        let keys = v7.keys;
        keys = v7.keys(v4);
        v7 = keys;
        v8 = ",";
        return v7.join(v8);
    }

    static testWhileBreak(param_0, param_1, param_2): void     {
        const v3 = 0;
        const v3 = 0;
        const v6 = v3;
        const v3: number = 1 + v6;
        for (v0 = v8; !(v6 > 100); v4) {
            const v7 = v3;
            const v6: number = 2 % v7;
            break;
            return;
            const v6 = v3;
            const v3: number = 1 + v6;
            continue;
        }
    }

    static get getEntryName(): void     {
        return "Index";
    }

    static testRecordAccess(param_0, param_1, param_2): void     {
        {  }.normal = "regular";
        v5 = v3;
        v6 = "extra";
        v5.value = v6;
        v6 = v3[v8];
        v5 = `|${v6}`;
        v7 = "extra";
        return v3[v7] + v5;
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

    static testStaticMembers(param_0, param_1, param_2): void     {
        let increment = v8.increment;
        increment = v8.increment();
        increment = v8.increment;
        increment = v8.increment();
        let count = v8.getCount;
        count = v8.getCount();
        let v11 = String;
        v11 = v11(increment);
        let v9: string = `,${v11}`;
        v11 = String;
        v11 = increment;
        let v8: number = v11(v11) + v9;
        count = `,${v8}`;
        v8 = String;
        v9 = count;
        return v8(v9) + count;
    }

    static testStringMethods(param_0, param_1, param_2): void     {
        const v6 = "hello world";
        let v10: string = v6;
        let indexOf = v10.indexOf;
        indexOf = v10.indexOf("world");
        v10 = v6;
        let includes = v10.includes;
        v11 = "hello";
        includes = v10.includes(v11);
        v10 = v6;
        let slice = v10.slice;
        v11 = 0;
        slice = v10.slice(v11, 5);
        v10 = "ab";
        let repeat = v10.repeat;
        v11 = 3;
        repeat = v10.repeat(v11);
        let v15 = String;
        v15 = v15(indexOf);
        const v13: string = `|${v15}`;
        v15 = String;
        v15 = includes;
        v12 = v15(v15) + v13;
        v11 = `|${v12}`;
        v10 = slice + v11;
        repeat = `|${v10}`;
        return repeat + repeat;
    }

    static testSwitchDefault(param_0, param_1, param_2, param_3): void     { }

    static testNestedTryCatch(param_0, param_1, param_2): void     {
        const v3 = "";
        const v7 = v3;
        const v6: string = `:${v7}`;
        const v3: string = `inner_catch${v6}`;
        const v7 = v3;
        const v6: string = `:${v7}`;
        const v3: string = `outer_catch${v6}`;
    }

    static updateStateVars(param_0, param_1, param_2, param_3): void     {
        return;
    }

    static testArrayFindFilter(param_0, param_1, param_2): void     {
        const v5: Array<unknown> = [];
        let v8: Array<unknown> = v5;
        let findIndex = v8.findIndex;
        findIndex = v8.findIndex(func_3);
        let filter = v8.filter;
        v9 = func_8;
        filter = v5.filter(v9);
        v9 = String;
        v9 = v9(findIndex);
        filter = `:${v9}`;
        v9 = String;
        v9 = filter;
        const length = v9.length;
        return v9(length) + filter;
    }

    static testTryCatchFinally(param_0, param_1, param_2): void     {
        const v7 = "catch:";
        const v8 = String;
        const v9 = v6;
        const v6 = undefined;
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

    static static_initializer(param_0, param_1, param_2): void     {
        v2 = v7;
        v2.tally = 0;
        return;
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