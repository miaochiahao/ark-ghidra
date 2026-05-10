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

    constructor(param_0, param_1, param_2, param_3, param_4)     {
        v2 = v9;
        let v6 = v3;
        super();
        v6 = super();
        v6.code = v11;
        throw acc;
    }

    static anonymous_method(param_0, param_1, param_2): void     {
        return;
    }

    static anonymous_method(param_0, param_1, param_2): void     {
        return new {  }();
    }

    static anonymous_method(param_0, param_1, param_2, param_3, param_4): void     {
        v7 = Column;
        let v8 = "100%";
        v7 = Column;
        v8 = "100%";
        return;
    }

    static count(param_0, param_1, param_2): void     {
        const v6 = v2;
        const __count = v6.__count;
        return __count.get();
    }

    static anonymous_method(param_0, param_1, param_2, param_3, param_4): void     {
        let v7 = If;
        v7 = lex_0_0;
        const count = v7.count;
        if (!(count < 0)) {
            const ifElseBranchUpdateFunction = v7.ifElseBranchUpdateFunction;
            const v8 = 1;
            const v9 = func_15;
            return;
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

    static anonymous_method(param_0, param_1, param_2, param_3, param_4): void     {
        let v8 = "Increment";
        v7 = Button;
        v8 = func_8;
        return;
    }

    static func_main_0(param_0, param_1, param_2): void     { }

    static async anonymous_method(param_0, param_1, param_2): void     {
        v5 = Text;
        return;
    }

    static anonymous_method(param_0, param_1, param_2): void     {
        const v5 = lex_0_0;
        v5.count = Number(v5.count) + 1;
        return;
    }

    static getCode(param_0, param_1, param_2): void     {
        const v4 = v2;
        return v4.code;
    }

    static anonymous_method(param_0, param_1, param_2, param_3): void     {
        let v7 = v2;
        const __count = v7.__count;
        return;
    }

    static message(param_0, param_1, param_2): void     {
        const v6 = v2;
        const __message = v6.__message;
        return __message.get();
    }

    static anonymous_method(param_0, param_1, param_2, param_3, param_4): void     {
        let v8 = "Count: ";
        v8 = String(lex_0_0.count) + v8;
        v7 = Text;
        v8 = 20;
        return;
    }

    static testDateOps(param_0, param_1, param_2): void     {
        const v11 = Date;
        let date = new Date(v11);
        let v12 = date;
        let fullYear = v12.getFullYear;
        fullYear = v12.getFullYear();
        v12 = date;
        let month = v12.getMonth;
        month = v12.getMonth();
        v12 = date;
        date = v12.getDate;
        date = v12.getDate();
        v12 = date;
        let hours = v12.getHours;
        hours = v12.getHours();
        v12 = date;
        let minutes = v12.getMinutes;
        minutes = v12.getMinutes();
        v12 = date;
        let time = v12.getTime;
        time = v12.getTime();
        let v19 = String;
        v19 = v19(fullYear);
        let v17: string = `-${v19}`;
        v19 = String;
        v19 = month;
        let v16: number = v19(v19) + v17;
        let v15: string = `-${v16}`;
        v16 = String;
        v17 = date;
        let v14: number = v16(v17) + v15;
        let v13: string = ` ${v14}`;
        v14 = String;
        v15 = hours;
        v12 = v14(v15) + v13;
        time = `:${v12}`;
        v12 = String;
        v13 = minutes;
        return v12(v13) + time;
    }

    static toString(param_0, param_1, param_2): void     {
        const v6 = "Error[";
        const v7 = String;
        const v8 = v2;
        const code = v8.code;
        let v5: number = v7(code) + v6;
        const v4: string = `]: ${v5}`;
        return v11.message + v4;
    }

    static rerender(param_0, param_1, param_2): void     {
        const v5 = v2;
        return;
    }

    static anonymous_method(param_0, param_1, param_2): void     {
        return;
    }

    static anonymous_method(param_0, param_1, param_2, param_3): void     {
        let v7 = v2;
        const __message = v7.__message;
        return;
    }

    static testErrorCatch(param_0, param_1, param_2): void     { }

    static testSetTimeout(param_0, param_1, param_2): void     {
        let v3 = 0;
        v3 = 42;
        return v3;
    }

    static testCustomError(param_0, param_1, param_2): void     {
        let v6 = "not found";
        v6 = new 404();
        return v6.toString();
    }

    static testObjectMerge(param_0, param_1, param_2): void     {
        {  }.test = "name";
        v7 = v3;
        v8 = "version";
        v7["1.0"] = v8;
        v7 = {  };
        v8 = "version";
        v7["2.0"] = v8;
        v7 = v5;
        v8 = "author";
        v7.dev = v8;
        v7 = {  };
        v8 = "name";
        v7[v9[v10]] = v8;
        v7 = v4;
        v8 = "version";
        v9 = v5;
        v10 = "version";
        v7[v9[v10]] = v8;
        v7 = v4;
        v8 = "author";
        v9 = v5;
        v10 = "author";
        v7[v9[v10]] = v8;
        v10 = v4[v12];
        v9 = `|${v10}`;
        v11 = "version";
        v8 = v4[v11] + v9;
        v7 = `|${v8}`;
        v9 = "author";
        return v4[v9] + v7;
    }

    static testSwitchBreak(param_0, param_1, param_2, param_3): void     { }

    static get getEntryName(): void     {
        return "Index";
    }

    static testFromCharCode(param_0, param_1, param_2): void     {
        throw v7;
    }

    static testStringSearch(param_0, param_1, param_2): void     {
        const v6 = "The quick brown fox jumps over the lazy dog";
        let v10: string = v6;
        let indexOf = v10.indexOf;
        indexOf = v10.indexOf("fox");
        v10 = v6;
        let lastIndexOf = v10.lastIndexOf;
        v11 = "dog";
        lastIndexOf = v10.lastIndexOf(v11);
        v10 = v6;
        let substring = v10.substring;
        v11 = indexOf;
        let v12 = indexOf;
        v12 = 3 + v12;
        substring = v10.substring(v11, v12);
        v10 = substring;
        let toUpperCase = v10.toUpperCase;
        toUpperCase = v10.toUpperCase();
        toUpperCase = indexOf;
        return lastIndexOf + toUpperCase;
    }

    static initialRender(param_0, param_1, param_2): void     {
        lex_0_0 = v10;
        let v5 = lex_0_0;
        v5 = lex_0_0;
        observeComponentCreation2 = v5.observeComponentCreation2;
        v6 = func_8;
        v7 = If;
        v5 = If;
        v5 = lex_0_0;
        observeComponentCreation2 = v5.observeComponentCreation2;
        v6 = func_19;
        v7 = Text;
        v5 = Text;
        pop = v5.pop;
        v5 = lex_0_0;
        observeComponentCreation2 = v5.observeComponentCreation2;
        v6 = func_30;
        v7 = Button;
        v5 = Button;
        pop = v5.pop;
        v5 = Column;
        pop = v5.pop;
        return;
    }

    static onCountChange(param_0, param_1, param_2, param_3, param_4): void     {
        const v6 = v2;
        let v9 = "Count changed from ";
        const v10 = String;
        const v11 = v3;
        let v8: number = v10(v11) + v9;
        const v7: string = ` to ${v8}`;
        v8 = String;
        v9 = v16;
        v6.message = v8(v9) + v7;
        return;
    }

    static testArrayCreation(param_0, param_1, param_2): void     {
        let v7: Array<unknown> = [];
        v7 = [];
        throw v8;
    }

    static testStringCompare(param_0, param_1, param_2, param_3, param_4): void     {
        const v6 = v3;
        if (!(v4 < v6)) {
        }
    }

    static updateStateVars(param_0, param_1, param_2, param_3): void     {
        return;
    }

    static testDateConstructor(param_0, param_1, param_2): void     {
        const v5 = Date;
        let v6 = 2024;
        v6 = new 1(v5);
        return v6.getFullYear();
    }

    static testNumberConstants(param_0, param_1, param_2): void     {
        v11 = Number;
        let v12 = Number;
        let safeInteger = v12.isSafeInteger;
        safeInteger = v12.isSafeInteger(42);
        v12 = Number;
        let naN = v12.isNaN;
        v13 = NaN;
        naN = v12.isNaN(v13);
        v12 = Number;
        let finite = v12.isFinite;
        v13 = Infinity;
        finite = v12.isFinite(v13);
        let v15 = String;
        v15 = v15(safeInteger);
        v13 = `|${v15}`;
        v15 = String;
        v15 = naN;
        v12 = v15(v15) + v13;
        finite = `|${v12}`;
        v12 = String;
        v13 = finite;
        return v12(v13) + finite;
    }

    static aboutToBeDeleted(param_0, param_1, param_2): void     {
        let v6 = v2;
        const __count = v6.__count;
        aboutToBeDeleted = v10.__message.aboutToBeDeleted;
        let v7 = SubscriberManager;
        get = v7.Get();
        id__ = v10.id__();
        get = v10;
        return;
    }

    static testNestedConditions(param_0, param_1, param_2, param_3, param_4): void     {
        const v6 = v3;
        if (!(v6 < 0)) {
        }
    }

    static setInitiallyProvidedValue(param_0, param_1, param_2, param_3): void     {
        const v6 = v3;
        const count = v6.count;
        if (count !== undefined) {
            const count = v2;
            const v6 = v3;
            count.count = v6.count;
        } else {
            const message = v6.message;
        }
        const message = v2;
        const v6 = v3;
        message.message = v6.message;
        return;
    }

    static purgeVariableDependenciesOnElmtId(param_0, param_1, param_2, param_3): void     {
        let v7 = v2;
        const __count = v7.__count;
        v7 = v11;
        v7 = v10;
        purgeDependencyOnElmtId = v7.__message.purgeDependencyOnElmtId;
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