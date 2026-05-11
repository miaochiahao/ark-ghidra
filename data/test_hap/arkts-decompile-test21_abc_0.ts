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
        v2._x = v10;
        v6 = v2;
        v6._y = v11;
        return v2;
    }

    static anonymous_method(param_0, param_1, param_2): void     {
        return;
    }

    static anonymous_method(param_0, param_1, param_2): void     {
        return new {  }();
    }

    static x(param_0, param_1, param_2): void     {
        const v4 = v2;
        return v4._x;
    }

    static y(param_0, param_1, param_2): void     {
        const v4 = v2;
        return v4._y;
    }

    static anonymous_method(param_0, param_1, param_2, param_3, param_4): void     {
        v7 = Column;
        let v8 = "100%";
        v7 = Column;
        v8 = "100%";
        return;
    }

    static area(param_0, param_1, param_2): void     {
        return 0;
    }

    static area(param_0, param_1, param_2): void     {
        let v6 = Math;
        let pI = v6.PI;
        const v4: number = pI * v6.radius;
        pI = v9;
        return v4 * pI.radius;
    }

    static area(param_0, param_1, param_2): void     {
        let v5 = v2;
        const w = v5.w;
        return w * v8.h;
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
        lex_0_4 = func_6;
    }

    static totalArea(param_0, param_1, param_2, param_3): void     {
        v3 = v16;
        const v7 = 0;
        v3 = v16;
        const v7 = 0;
        const v11 = v3;
        const v12 = v7;
        const v10 = v11[v12];
        v8 = v7;
        const number: number = Number(v8);
        const v7: number = number + 1;
        for (v0 = v13; !(v8 < v9.length); v8) {
            const v9 = v3;
        }
    }

    static message(param_0, param_1, param_2): void     {
        const v6 = v2;
        const __message = v6.__message;
        return __message.get();
    }

    static describe(param_0, param_1, param_2): void     {
        const v8 = v2;
        let name = v8.name;
        const v6: string = `${name}: area=`;
        name = String;
        const v9 = v2;
        let area = v9.area;
        area = v9.area();
        let v5: number = v6 + name(area);
        const v4: string = `${v5}, perimeter=`;
        v5 = String;
        name = v12;
        let perimeter = name.perimeter;
        perimeter = name.perimeter();
        return v4 + v5(perimeter);
    }

    static diameter(param_0, param_1, param_2): void     {
        const v4 = 2;
        const v5 = v2;
        return v4 * v5.radius;
    }

    static isSquare(param_0, param_1, param_2): void     {
        let v5 = v2;
        const w = v5.w;
        return w === v8.h;
    }

    static createSq(param_0, param_1, param_2, param_3): void     {
        return new v10();
    }

    static rerender(param_0, param_1, param_2): void     {
        const v5 = v2;
        return;
    }

    static testDescribe(param_0, param_1, param_2): void     {
        let v6 = 3;
        v6 = new 4();
        return v6.describe();
    }

    static perimeter(param_0, param_1, param_2): void     {
        return 0;
    }

    static shapeName(param_0, param_1, param_2): void     {
        const v4 = v2;
        return v4.name;
    }

    static perimeter(param_0, param_1, param_2): void     {
        let v5 = 2;
        const v6 = Math;
        const v4: number = v5 * v6.PI;
        return v4 * v9.radius;
    }

    static perimeter(param_0, param_1, param_2): void     {
        const v4 = 2;
        let v6 = v2;
        const w = v6.w;
        return v4 * (w + v9.h);
    }

    static anonymous_method(param_0, param_1, param_2, param_3): void     {
        let v7 = v2;
        const __message = v7.__message;
        return;
    }

    static distanceTo(param_0, param_1, param_2, param_3): void     {
        let v8 = v2;
        const _x = v8._x;
        v8 = v14;
        const v4: number = _x - v8.x;
        v8 = v13;
        const _y = v8._y;
        v8 = v14;
        const v5: number = _y - v8.y;
        v8 = Math;
        let v10: number = v4;
        const v9: number = v10 * v4;
        v9 += v5 * v5;
        return v8.sqrt(v9);
    }

    static anonymous_method(param_0, param_1, param_2, param_3): void     {
        v2 = v8;
        v2.name = v9;
        return;
    }

    static getEntryName(param_0, param_1, param_2): void     {
        return "Index";
    }

    static testGetterSetter(param_0, param_1, param_2): void     {
        let v8 = 3;
        const v3 = new 3();
        let v7 = v3;
        let shapeName = v7.shapeName;
        v7 = v3;
        v7.shapeName = "MyRoundShape";
        v7 = v3;
        shapeName = v7.shapeName;
        v8 = shapeName;
        v7 = `${v8},`;
        return v7 + shapeName;
    }

    static testPolymorphism(param_0, param_1, param_2): void     {
        let v6: Array<unknown> = [];
        let v8 = 1;
        new 1(v7)[6] = v10;
        v8 = 2;
        new 3(v7, v8, 3, v10, v11)[6] = v11;
        v8 = 4;
        new 4(v7, v8, v9, v10, v11, v12, v13, v14, v15)[6] = v12;
        const v3: Array<unknown> = v6;
        v6 = lex_0_4;
        let v7: Array<unknown> = v3;
        v6 = v6(v7);
        v6 = String;
        return v6(v6);
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

    static createBoxShape(param_0, param_1, param_2, param_3, param_4): void     {
        return new v13();
    }

    static testImmutablePoint(param_0, param_1, param_2): void     {
        let v8 = 0;
        let v9 = 0;
        const v4 = new 0();
        v8 = 3;
        v9 = 4;
        const v5 = new 4(v7, v8);
        v8 = v4;
        let distanceTo = v8.distanceTo;
        distanceTo = v8.distanceTo(v5);
        distanceTo = String;
        v8 = distanceTo;
        return distanceTo(v8);
    }

    static updateStateVars(param_0, param_1, param_2, param_3): void     {
        return;
    }

    static createRoundShape(param_0, param_1, param_2, param_3): void     {
        return new v10();
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

    static testShapeInheritance(param_0, param_1, param_2): void     {
        let createRoundShape = v13.createRoundShape;
        createRoundShape = v13.createRoundShape(5);
        let createBoxShape = v13.createBoxShape;
        v14 = 3;
        createBoxShape = v13.createBoxShape(v14, 4);
        let createSq = v13.createSq;
        v14 = 2;
        createSq = v13.createSq(v14);
        let v13 = createRoundShape;
        let area = v13.area;
        area = v13.area();
        v13 = createBoxShape;
        area = v13.area;
        area = v13.area();
        v13 = createSq;
        area = v13.area;
        area = v13.area();
        area = createRoundShape;
        const diameter = area.diameter;
        v13 = createSq;
        let square = v13.isSquare;
        square = v13.isSquare();
        let v20 = String;
        v20 = v20(area);
        let v18: string = `${v20},`;
        v20 = String;
        v20 = area;
        let v17: number = v18 + v20(v20);
        let v16: string = `${v17},`;
        v17 = String;
        v18 = area;
        v15 = v16 + v17(v18);
        v14 = `${v15},`;
        v15 = String;
        v16 = diameter;
        v13 = v14 + v15(v16);
        square = `${v13},`;
        v13 = String;
        v14 = square;
        return square + v13(v14);
    }

    static testMultiLevelInheritance(param_0, param_1, param_2): void     {
        let v9 = 5;
        v9 = new 5();
        let perimeter = v9.perimeter;
        perimeter = v9.perimeter();
        v9 = v6;
        let square = v9.isSquare;
        square = v9.isSquare();
        square = v6;
        const shapeName = square.shapeName;
        let v12 = String;
        v12 = v12(perimeter);
        const v10: string = `${v12},`;
        v12 = String;
        v12 = square;
        v9 = v10 + v12(v12);
        square = `${v9},`;
        return square + shapeName;
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