export class ResourceTable& {
    pkgName@entry;

    static func_main_0(param_0, param_1, param_2): void     {
        return;
    }
}
export class EntryAbility& {
    pkgName@entry;

    constructor(param_0, param_1, param_2, param_3)     {
        throw v6;
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
        throw v6;
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
        v2.label = v9;
        return v2;
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

    static isString(param_0, param_1, param_2, param_3): void     {
        const v5: string = typeof v3;
        return v5 === "string";
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
        lex_0_0 = func_0;
        v13 = func_1;
        lex_0_1 = v13;
    }

    static testForOf(param_0, param_1, param_2): void     {
        throw "";
    }

    static async asyncFetch(param_0, param_1, param_2, param_3): void     {
        throw v4;
    }

    static message(param_0, param_1, param_2): void     {
        const v6 = v2;
        const __message = v6.__message;
        return __message.get();
    }

    static getLabel(param_0, param_1, param_2): void     {
        const v4 = v2;
        return v4.label;
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

    static testNestedMap(param_0, param_1, param_2): void     {
        let v7 = Map;
        let map = new Map(v7);
        v7 = Map;
        map = new Map(v7, v8, v9, v10);
        let v8 = map2;
        let v9 = "x";
        let v10 = 100;
        v8 = map2;
        set = v8.set;
        v9 = "y";
        v10 = 200;
        v8 = map;
        set = v8.set;
        v9 = "point";
        v10 = map2;
        v8 = map;
        v9 = "point";
        get = v8.get(v9);
        get = get2;
    }

    static async testAsyncAwait(param_0, param_1, param_2): void     {
        const v5 = v4;
        const v6 = "caught:";
        v6 += v5.message;
        return v3;
    }

    static testInstanceOf(param_0, param_1, param_2): void     {
        let v5 = Error;
        let v6 = "test";
        v5 = new Error();
        v5 = false;
        return v5.message;
    }

    static testSplitLimit(param_0, param_1, param_2): void     {
        const v3 = "a,b,c,d,e";
        let v9: string = v3;
        let split = v9.split;
        split = v9.split(",");
        split = split2;
        v9 = 0;
        const v4 = split[v9];
        split = split2;
        v9 = split2.length - 1;
        const v5 = split[v9];
        split = `${v4},`;
        return split + v5;
    }

    static testArrayBuffer(param_0, param_1, param_2): void     {
        let v10 = ArrayBuffer;
        let v11 = 4;
        let arrayBuffer = new ArrayBuffer();
        v10 = DataView;
        v11 = arrayBuffer;
        arrayBuffer = new arrayBuffer(v10, v11, v12, v13);
        v11 = arrayBuffer2;
        let uint8 = v11.setUint8;
        let v12 = 0;
        let v13 = 72;
        v11 = arrayBuffer2;
        uint8 = v11.setUint8;
        v12 = 1;
        v13 = 101;
        v11 = arrayBuffer2;
        uint8 = v11.setUint8;
        v12 = 2;
        v13 = 108;
        v11 = arrayBuffer2;
        uint8 = v11.setUint8;
        v12 = 3;
        v13 = 111;
        v11 = arrayBuffer2;
        uint8 = v11.getUint8;
        v12 = 0;
        uint8 = v11.getUint8(v12);
        v11 = arrayBuffer2;
        uint8 = v11.getUint8;
        v12 = 1;
        uint8 = v11.getUint8(v12);
        v11 = arrayBuffer2;
        uint8 = v11.getUint8;
        v12 = 2;
        uint8 = v11.getUint8(v12);
        v11 = arrayBuffer2;
        uint8 = v11.getUint8;
        v12 = 3;
        uint8 = v11.getUint8(v12);
        let v16 = String;
        v16 = v16(uint82);
        let v14: string = `${v16},`;
        v16 = String;
        v16 = uint83;
        v13 = v14 + v16(v16);
        v12 = `${v13},`;
        v13 = String;
        v14 = uint84;
        v11 = v12 + v13(v14);
        uint8 = `${v11},`;
        v11 = String;
        v12 = uint85;
        return uint8 + v11(v12);
    }

    static getEntryName(param_0, param_1, param_2): void     {
        return "Index";
    }

    static testPatternMatch(param_0, param_1, param_2): void     {
        const v7 = "Hello, World!";
        let v10: string = v7;
        let indexOf = v10.indexOf;
        indexOf = v10.indexOf("World");
        indexOf = indexOf2;
        const v5: boolean = indexOf >= 0;
        v10 = v7;
        let substring = v10.substring;
        v11 = 0;
        substring = v10.substring(v11, 5);
        v10 = v7;
        substring = v10.substring;
        v11 = 7;
        v12 = 12;
        substring = v10.substring(v11, v12);
        let v13 = String;
        v13 = v13(v5);
        v11 = `${v13},`;
        v10 = v11 + substring2;
        substring = `${v10},`;
        return substring + substring3;
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

    static testTypePredicate(param_0, param_1, param_2): void     {
        const v5 = "hello";
        const v6 = 42;
        let v8 = lex_0_1;
        v8 = v8(v5);
        v8 = lex_0_1;
        v8 = v8(v6);
        let v10 = String;
        v10 = v10(v8);
        v8 = `${v10},`;
        v10 = String;
        return v8 + v82(v82);
    }

    static updateStateVars(param_0, param_1, param_2, param_3): void     {
        return;
    }

    static aboutToBeDeleted(param_0, param_1, param_2): void     {
        const v6 = v2;
        const __message = v6.__message;
        let v7 = SubscriberManager;
        get = v7.Get();
        id__ = v10.id__();
        const get2 = v2;
        return;
    }

    static testChainedMethodCall(param_0, param_1, param_2): void     {
        const v3 = new "box"();
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